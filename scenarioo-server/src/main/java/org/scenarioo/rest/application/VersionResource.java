package org.scenarioo.rest.application;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.scenarioo.dao.aggregates.AggregatedDocuDataReader;
import org.scenarioo.dao.aggregates.ScenarioDocuAggregationDao;
import org.scenarioo.dao.version.ApplicationVersion;
import org.scenarioo.dao.version.ApplicationVersionHolder;
import org.scenarioo.repository.ConfigurationRepository;
import org.scenarioo.repository.RepositoryLocator;
import org.scenarioo.rest.feature.FeaturesResource;

@Path("/rest/version/")
public class VersionResource {

	private static final Logger LOGGER = Logger.getLogger(FeaturesResource.class);

	private final ConfigurationRepository configurationRepository = RepositoryLocator.INSTANCE
			.getConfigurationRepository();

	AggregatedDocuDataReader dao = new ScenarioDocuAggregationDao(configurationRepository.getDocumentationDataDirectory());

	@GET
	@Produces({ "application/xml", "application/json" })
	public ApplicationVersion getVersionInformation() {
		LOGGER.info("REQUEST: getVersionInformation()");
		return ApplicationVersionHolder.INSTANCE.getApplicationVersion();
	}

}
