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
angular.module('scenarioo.services').service('LocalStorageNameService', function () {
    var service = this;

    service.LATEST_VIEW_NAME = 'latestView';
    localStorage.setItem(service.LATEST_VIEW_NAME, 'feature');

    service.CURRENT_FEATURE = 'currentFeature';

    service.SUCCESS = 'success';

    service.FAILED = 'failed';

    service.DEFAULT_VIEW = 'defaultView';

    return service;
});
