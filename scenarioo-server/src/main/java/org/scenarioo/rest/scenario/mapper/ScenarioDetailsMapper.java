package org.scenarioo.rest.scenario.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scenarioo.model.docu.aggregates.scenarios.PageSteps;
import org.scenarioo.model.docu.aggregates.scenarios.ScenarioPageSteps;
import org.scenarioo.model.docu.entities.Page;
import org.scenarioo.rest.scenario.dto.PageSummary;
import org.scenarioo.rest.scenario.dto.PageWithSteps;
import org.scenarioo.rest.scenario.dto.ScenarioDetails;

public class ScenarioDetailsMapper {

	public ScenarioDetails map(final ScenarioPageSteps pageSteps) {
		if (pageSteps == null) {
			return null;
		}

		ScenarioDetails scenarioDetails = new ScenarioDetails();
		scenarioDetails.setPagesAndSteps(mapPages(pageSteps.getPagesAndSteps()));
		scenarioDetails.setScenario(pageSteps.getScenario());
		scenarioDetails.setScenarioStatistics(pageSteps.getScenarioStatistics());
		scenarioDetails.setFeature(pageSteps.getFeature());
		return scenarioDetails;
	}

	private List<PageWithSteps> mapPages(final List<PageSteps> pagesAndSteps) {
		if (pagesAndSteps == null) {
			return null;
		}

		Map<String, Integer> occurrences = new HashMap<String, Integer>();

		List<PageWithSteps> pages = new ArrayList<PageWithSteps>(pagesAndSteps.size());
		for (PageSteps pageStep : pagesAndSteps) {
			pages.add(mapPage(pageStep, getPageOccurrenceForPageName(occurrences, pageStep.getPage().getName())));
		}
		return pages;
	}

	private int getPageOccurrenceForPageName(final Map<String, Integer> occurrences, final String pageName) {
		Integer occurrencesSoFar = occurrences.get(pageName);
		if (occurrencesSoFar == null) {
			occurrences.put(pageName, 1);
			return 0;
		} else {
			int thisOccurrence = occurrencesSoFar + 1;
			occurrences.put(pageName, thisOccurrence);
			return thisOccurrence - 1; // because it is a 0-based index
		}
	}

	private PageWithSteps mapPage(final PageSteps pageStep, final int pageOccurrence) {
		PageWithSteps pageWithSteps = new PageWithSteps();
		pageWithSteps.setPage(mapPageSummary(pageStep.getPage(), pageOccurrence));
		pageWithSteps.setSteps(pageStep.getSteps());
		return pageWithSteps;
	}

	private PageSummary mapPageSummary(final Page page, final int pageOccurrence) {
		PageSummary pageSummary = new PageSummary();
		pageSummary.setName(page.getName());
		pageSummary.setPageOccurrence(pageOccurrence);
		return pageSummary;
	}

}
