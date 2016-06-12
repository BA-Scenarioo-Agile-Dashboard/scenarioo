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

package org.scenarioo.business.diffViewer.comparator;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.scenarioo.dao.aggregates.AggregatedDocuDataReader;
import org.scenarioo.dao.aggregates.ScenarioDocuAggregationDao;
import org.scenarioo.model.configuration.ComparisonConfiguration;
import org.scenarioo.model.diffViewer.ScenarioDiffInfo;
import org.scenarioo.model.diffViewer.UseCaseDiffInfo;
import org.scenarioo.model.docu.aggregates.usecases.ScenarioSummary;
import org.scenarioo.model.docu.aggregates.usecases.UseCaseScenarios;
import org.scenarioo.model.docu.entities.Scenario;
import org.scenarioo.rest.base.BuildIdentifier;

/**
 * Comparison results are persisted in a xml file.
 */
public class ScenarioComparator extends AbstractComparator {

	private static final Logger LOGGER = Logger.getLogger(ScenarioComparator.class);

	private StepComparator stepComparator = new StepComparator(baseBranchName, baseBuildName, comparisonConfiguration);

	private AggregatedDocuDataReader aggregatedDataReader = new ScenarioDocuAggregationDao(
			configurationRepository.getDocumentationDataDirectory());

	public ScenarioComparator(final String baseBranchName, final String baseBuildName,
			final ComparisonConfiguration comparisonConfiguration) {
		super(baseBranchName, baseBuildName, comparisonConfiguration);
	}

	public UseCaseDiffInfo compare(final String baseUseCaseName) {
		final List<Scenario> baseScenarios = docuReader.loadScenarios(baseBranchName, baseBuildName, baseUseCaseName);
		final List<Scenario> comparisonScenarios = docuReader.loadScenarios(
				comparisonConfiguration.getComparisonBranchName(),
				comparisonConfiguration.getComparisonBuildName(), baseUseCaseName);

		final UseCaseDiffInfo useCaseDiffInfo = new UseCaseDiffInfo(baseUseCaseName);
		double scenarioChangeRateSum = 0;

		for (final Scenario baseScenario : baseScenarios) {
			if (StringUtils.isEmpty(baseScenario.getName())) {
				throw new RuntimeException("Found empty name for scenario.");
			}

			final Scenario comparisonScenario = getScenarioByName(comparisonScenarios, baseScenario.getName());
			if (comparisonScenario == null) {
				LOGGER.debug("Found new scenario called [" + baseScenario.getName() + "] in base branch ["
						+ baseBranchName + "] and base build [" + baseBuildName + "] and base use case ["
						+ baseUseCaseName + "]");
				useCaseDiffInfo.setAdded(useCaseDiffInfo.getAdded() + 1);
				useCaseDiffInfo.getAddedElements().add(baseScenario.getName());
			} else {
				comparisonScenarios.remove(comparisonScenario);

				final ScenarioDiffInfo scenarioDiffInfo = stepComparator.compare(baseUseCaseName, baseScenario.getName());

				diffWriter.saveScenarioDiffInfo(scenarioDiffInfo, baseUseCaseName);

				if (scenarioDiffInfo.hasChanges()) {
					useCaseDiffInfo.setChanged(useCaseDiffInfo.getChanged() + 1);
					scenarioChangeRateSum += scenarioDiffInfo.getChangeRate();
				}
			}
		}
		LOGGER.debug(comparisonScenarios.size() + " scenarios were removed in base branch ["
				+ baseBranchName + "] and base build [" + baseBuildName + "] and base use case ["
				+ baseUseCaseName + "]");
		useCaseDiffInfo.setRemoved(comparisonScenarios.size());
		useCaseDiffInfo.setRemovedElements(
				getScenarioSummaries(comparisonScenarios, baseUseCaseName));
		useCaseDiffInfo.setChangeRate(calculateChangeRate(baseScenarios.size(), useCaseDiffInfo.getAdded(),
				useCaseDiffInfo.getRemoved(), scenarioChangeRateSum));

		return useCaseDiffInfo;
	}

	private List<ScenarioSummary> getScenarioSummaries(final List<Scenario> scenarios, final String useCaseName) {
		final List<ScenarioSummary> scenarioSummaries = new LinkedList<ScenarioSummary>();

		if (scenarios.isEmpty()) {
			return scenarioSummaries;
		}

		final BuildIdentifier comparisonBuildIdentifier = new BuildIdentifier(
				comparisonConfiguration.getComparisonBranchName(),
				comparisonConfiguration.getComparisonBuildName());
		final UseCaseScenarios useCaseScenarios = aggregatedDataReader.loadUseCaseScenarios(comparisonBuildIdentifier,
				useCaseName);

		for (final Scenario scenario : scenarios) {
			for (final ScenarioSummary scenarioSummary : useCaseScenarios.getScenarios()) {
				if (scenario.getName().equals(scenarioSummary.getScenario().getName())) {
					scenarioSummaries.add(scenarioSummary);
				}
			}
		}

		return scenarioSummaries;
	}

	private Scenario getScenarioByName(final List<Scenario> scenarios, final String scenarioName) {
		for (final Scenario scenario : scenarios) {
			if (scenarioName.equals(scenario.getName())) {
				return scenario;
			}
		}
		return null;
	}

}
