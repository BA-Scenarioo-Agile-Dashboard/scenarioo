/* scenarioo-client
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

angular.module('scenarioo.services').factory('BreadcrumbsService', function ($filter) {

    /**
     *  Configure breadcrumb elements for different objects that can occur in a concrete breadcrumb path for a page
     */

    var homeElement =
    {
        label: '<i class="icon-home"></i> Home',
        route: '/feature/?feature=Home'             // maybe better rename to linkUrl
    };

    var manageElement =
    {
        label: '<i class="icon-cogs"></i> Manage',
        route: 'manage/manage.html'
    };

    var featureElement =
    {
        label: '<strong>Feature:</strong> [feature]',
        route: '/feature/:feature/'
    };

    var scenarioElement =
    {
        label: '<strong>Scenario:</strong> [scenario]',
        route: '/scenario/:feature/:scenario/'
    };

    var stepElement =
    {
        label: '<strong>Step:</strong> [pageName]/[pageOccurrence]/[stepInPageOccurrence]',
        route: '/step/:feature/:scenario/:pageName/:pageOccurrence/:stepInPageOccurrence/'
    };

    var objectElement =
    {
        label: '<strong>[objectType]:</strong> [objectName]',
        route: '/object/:objectType/:objectName'
    };

    var stepSketchElement =
    {
        label: '<strong>Sketch</strong>'
    };

    var searchElement =
    {
        label: '<strong>Search Results for [searchTerm]</strong>'
    };

    var dashboard =
        {
            label: '<strong>Map</strong>',
            route: '/dashboard/:feature'
        };
    var detail =
        {
            label: '<strong>Detail</strong>',
            route: '/detailNav/:feature'
        };
    var feature =
        {
            label: '<strong>Feature</strong>',
            route: '/feature/:feature'
        };
    var testScenarios =
        {
            label: 'Scenarios:',
            route: '/testScenarios'
        };

    /**
     *  Configure breadcrumb paths that can be assigned to routes (see app.js) to display them as breadcrumbs for according pages.
     *  Key of the elements is the 'breadcrumbId', use it to link one of this path to a routing in app.js
     */
    var breadcrumbPaths = {

        'feature': {
            breadcrumbPath: [featureElement]
        },

        'scenario': {
            breadcrumbPath: [scenarioElement] // featureElement replaced with testScenarios
        },

        'step': {
            breadcrumbPath: [scenarioElement, stepElement]  // featureElement replaced with testScenarios
        },

        'main': {
            breadcrumbPath: [homeElement]
        },

        'object': {
            breadcrumbPath: [objectElement]
        },

        'manage': {
            breadcrumbPath: [manageElement]
        },

        'stepsketch': {
            breadcrumbPath: [stepSketchElement]
        },

        'search': {
            breadcrumbPath: [searchElement]
        },

        'dashboard': {
            breadcrumbPath: []
        },
        'detail': {
            breadcrumbPath: []
        },
        'feature': {
            breadcrumbPath: []
        },
        'testScenarios': {
            breadcrumbPath: []
        }
    };

    function convertToPlainText(text) {
        return text.replace(/<\/?[^>]+(>|$)/g, '');
    }

    function setValuesInRoute(text, navParameter) {
        var placeholders = text.match(/:.*?[^<](?=\/)/g);

        if (placeholders !== null) {
            angular.forEach(placeholders, function (placeholder) {
                placeholder = placeholder.replace(':', '');
                if (placeholder === 'feature' || placeholder === 'scenario' || placeholder === 'issueId' || placeholder === 'scenarioSketchId') {
                    text = text.replace(':' + placeholder, navParameter[placeholder]);
                }
            });
        }
        return text;
    }

    function getText(navParameter, placeholder) {
        var value = navParameter[placeholder];

        if (placeholder === 'feature' || placeholder === 'scenario') {
            return $filter('scHumanReadable')(value);
        }

        return value;
    }

    function setValuesInLabel(text, navParameter) {
        var placeholders = text.match(/\[.*?(?=])./g);

        if (placeholders !== null) {
            angular.forEach(placeholders, function (placeholder) {
                placeholder = placeholder.replace(/[\[\]]/g, '');
                text = text.replace(/[\[\]]/g, '');
                text = text.replace(placeholder, getText(navParameter, placeholder));
            });
            text = decodeURIComponent(text);
        }
        return text;
    }

    function buildNavigationElements(breadcrumbId, navParameters) {
        if (angular.isUndefined(breadcrumbId) || angular.isUndefined(navParameters)) {
            return undefined;
        }

        var breadCrumbElements = angular.copy(breadcrumbPaths[breadcrumbId]);

        var navElements = [];

        if (angular.isDefined(breadCrumbElements)) {
            angular.forEach(breadCrumbElements.breadcrumbPath, function (navigationElement, index) {
                if ((breadCrumbElements.breadcrumbPath.length - index) === 1) {
                    navigationElement.isLastNavigationElement = true;
                }
                else {
                    navigationElement.isLastNavigationElement = false;
                }
                if(navigationElement.route) {
                    navigationElement.route = setValuesInRoute(navigationElement.route, navParameters);
                }
                navigationElement.label = setValuesInLabel(navigationElement.label, navParameters);
                navigationElement.textForTooltip = convertToPlainText(navigationElement.label);
                navElements.push(navigationElement);
            });
        }
        return navElements;
    }

    // Service interface
    return {
        getNavigationElements: buildNavigationElements
    };

});
