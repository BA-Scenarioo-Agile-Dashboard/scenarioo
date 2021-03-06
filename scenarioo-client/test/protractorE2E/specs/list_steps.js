'use strict';

var scenarioo = require('scenarioo-js');
var pages = require('./../webPages');

useCase('List_steps_of_scenario')
    .description('Gives an overview of all steps in a scenario.')
    .describe(function () {

        var homePage = new pages.homePage();
        var stepPage = new pages.stepPage();
        var scenarioPage = new pages.scenarioPage();
        var navigationPage = new pages.navigationPage();

        beforeEach(function () {
            homePage.initLocalStorage();
        });

        scenario('ScenarioPage with comparisons')
            .description('Displaying diff info icons.')
            .labels(['diff-viewer'])
            .it(function () {
                scenarioPage.goToPage('/scenario/Donate/find_donate_page?branch=wikipedia-docu-example&build=2014-03-19');
                navigationPage.chooseComparison('To Projectstart');
                scenarioPage.expandAllPages();
                scenarioPage.assertFirstChangedPageDiffIconHasValue();
                scenarioPage.assertFirstChangedStepDiffIconHasValue();
                scenarioPage.assertAddedStepDiffIconTextEqualsAdded();
                scenarioPage.assertRemovedStepDiffIconTextEqualsRemoved();
                scenarioPage.assertAddedPageDiffIconTextEqualsAdded();
                scenarioPage.assertRemovedPageDiffIconTextEqualsRemoved();
                step('Display one scenario');
                navigationPage.disableComparison();
            });
    });

