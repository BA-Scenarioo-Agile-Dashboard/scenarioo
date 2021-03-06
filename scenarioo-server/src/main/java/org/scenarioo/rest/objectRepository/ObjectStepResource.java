/* scenarioo-server
 * Copyright (C) 2014, scenarioo.org Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.scenarioo.rest.objectRepository;

import org.scenarioo.business.builds.ScenarioDocuBuildsManager;
import org.scenarioo.dao.aggregates.AggregatedDocuDataReader;
import org.scenarioo.dao.aggregates.ScenarioDocuAggregationDao;
import org.scenarioo.model.docu.aggregates.objects.LongObjectNamesResolver;
import org.scenarioo.model.docu.aggregates.objects.ObjectIndex;
import org.scenarioo.model.docu.entities.generic.ObjectList;
import org.scenarioo.model.docu.entities.generic.ObjectReference;
import org.scenarioo.model.docu.entities.generic.ObjectTreeNode;
import org.scenarioo.repository.ConfigurationRepository;
import org.scenarioo.repository.RepositoryLocator;
import org.scenarioo.rest.base.AbstractBuildContentResource;
import org.scenarioo.rest.base.BuildIdentifier;
import org.scenarioo.rest.base.StepIdentifier;
import org.scenarioo.rest.step.logic.ScenarioLoader;
import org.scenarioo.rest.step.logic.StepIndexResolver;
import org.scenarioo.rest.step.logic.StepLoader;
import org.scenarioo.rest.step.logic.StepLoaderResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resource for getting the URLs for screenshots for a certain object
 */
@Path("/rest/branch/{branchName}/build/{buildName}/object/{type}/{objectName}")
public class ObjectStepResource extends AbstractBuildContentResource {

	private final ConfigurationRepository configurationRepository = RepositoryLocator.INSTANCE
		.getConfigurationRepository();

	private final LongObjectNamesResolver longObjectNamesResolver = new LongObjectNamesResolver();

	private final AggregatedDocuDataReader aggregatedDataReader = new ScenarioDocuAggregationDao(
		configurationRepository.getDocumentationDataDirectory(), longObjectNamesResolver);

	private final ScenarioLoader scenarioLoader = new ScenarioLoader(aggregatedDataReader);
	private final StepIndexResolver stepIndexResolver = new StepIndexResolver();
	private final StepLoader stepLoader = new StepLoader(scenarioLoader, stepIndexResolver);

	/**
	 * Get references to all steps, that contain a specific object.
	 *
	 * @param branchName branch to search in
	 * @param buildName  build to searhc in
	 * @param objectType type of the object
	 * @param objectName name of the object
	 * @return a flat list of step reference objects
	 */
	@GET
	@Path("/steps")
	@Produces({"application/json"})
	public List<StepReference> getStepReferences(@PathParam("branchName") final String branchName,
												 @PathParam("buildName") final String buildName,
												 @PathParam("type") final String objectType,
												 @PathParam("objectName") final String objectName) throws IOException {

		List<StepLoaderResult> stepLoaderResults = getRelatedSteps(branchName, buildName, objectType, objectName);
		return transformStepsToStepReferences(stepLoaderResults);
	}

	private List<StepLoaderResult> getRelatedSteps(@PathParam("branchName") String branchName, @PathParam("buildName") String buildName, @PathParam("type") String objectType, @PathParam("objectName") String objectName) {
		BuildIdentifier buildIdentifier = ScenarioDocuBuildsManager.INSTANCE.resolveBranchAndBuildAliases(branchName,
			buildName);

		ObjectIndex objectIndex = aggregatedDataReader.loadObjectIndex(buildIdentifier, objectType, objectName);
		List<StepLoaderResult> stepLoaderResults = new ObjectList<StepLoaderResult>();
		for (ObjectTreeNode<Object> featureTreeNode : objectIndex.getReferenceTree().getChildren()) {
			visitFeature(buildIdentifier, stepLoaderResults, featureTreeNode);
		}
		return stepLoaderResults;
	}

	private void visitFeature(BuildIdentifier buildIdentifier, List<StepLoaderResult> stepLoaderResults, ObjectTreeNode<Object> featureTreeNode) {
		if (featureTreeNode.getItem() instanceof ObjectReference) {
			ObjectReference featureItem = (ObjectReference) featureTreeNode.getItem();
			if (featureItem.getType().equals("feature")) {
				for (ObjectTreeNode<Object> scenarioTreeNode : featureTreeNode.getChildren()) {
					visitScenario(buildIdentifier, stepLoaderResults, featureItem, scenarioTreeNode);
				}
			}
		}
	}

	private void visitScenario(BuildIdentifier buildIdentifier, List<StepLoaderResult> stepLoaderResults, ObjectReference featureItem, ObjectTreeNode<Object> scenarioTreeNode) {
		if (scenarioTreeNode.getItem() instanceof ObjectReference) {
			ObjectReference scenarioTreeNodeItem = (ObjectReference) scenarioTreeNode.getItem();
			if (scenarioTreeNodeItem.getType().equals("scenario")) {
				for (ObjectTreeNode<Object> stepTreeNode : scenarioTreeNode.getChildren()) {
					visitStep(buildIdentifier, stepLoaderResults, featureItem, scenarioTreeNodeItem, stepTreeNode);
				}
			}
		}
	}

	private void visitStep(BuildIdentifier buildIdentifier, List<StepLoaderResult> stepLoaderResults, ObjectReference featureItem, ObjectReference scenarioTreeNodeItem, ObjectTreeNode<Object> stepTreeNode) {
		if (stepTreeNode.getItem() instanceof ObjectReference) {
			ObjectReference stepItem = (ObjectReference) stepTreeNode.getItem();
			if (stepItem.getType().equals("step")) {
				String[] split = stepItem.getName().split(Pattern.quote("/"));
				StepIdentifier stepIdentifier = new StepIdentifier(buildIdentifier, featureItem.getName(),
					scenarioTreeNodeItem.getName(), split[0],
					Integer.parseInt(split[1]), Integer.parseInt(split[2]));
				StepLoaderResult stepLoaderResult = stepLoader.loadStep(stepIdentifier);
				stepLoaderResults.add(stepLoaderResult);
			}
		}
	}

	private List<StepReference> transformStepsToStepReferences(List<StepLoaderResult> stepLoaderResults) throws IOException {
		ArrayList<StepReference> stepDetailUrls = new ArrayList<StepReference>();
		for (StepLoaderResult stepLoaderResult : stepLoaderResults) {
			String stepDetailUrl = generateStepDetailLink(stepLoaderResult);
			String screenshotUrl = generateScreenshotLink(stepLoaderResult);
			stepDetailUrls.add(new StepReference(stepDetailUrl, screenshotUrl));
		}
		return stepDetailUrls;
	}

	private String generateScreenshotLink(StepLoaderResult stepLoaderResult) {
		return String.format("rest/branch/%s/build/%s/feature/%s/scenario/%s/image/%s",
			stepLoaderResult.getStepIdentifier().getBranchName(),
			stepLoaderResult.getStepIdentifier().getBuildName(),
			stepLoaderResult.getStepIdentifier().getFeatureName(),
			stepLoaderResult.getStepIdentifier().getScenarioName(),
			stepLoaderResult.getScreenshotFileName());
	}

	private String generateStepDetailLink(StepLoaderResult stepLoaderResult) {
		return String.format("rest/branch/%s/build/%s/feature/%s/scenario/%s/pageName/%s/pageOccurrence/%d/stepInPageOccurrence/%d",
			stepLoaderResult.getStepIdentifier().getBranchName(),
			stepLoaderResult.getStepIdentifier().getBuildName(),
			stepLoaderResult.getStepIdentifier().getFeatureName(),
			stepLoaderResult.getStepIdentifier().getScenarioName(),
			stepLoaderResult.getStepIdentifier().getPageName(),
			stepLoaderResult.getStepIdentifier().getPageOccurrence(),
			stepLoaderResult.getStepIdentifier().getStepInPageOccurrence());
	}
}
