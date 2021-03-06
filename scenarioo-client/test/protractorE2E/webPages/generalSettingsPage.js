'use strict';

var BaseWebPage = require('./baseWebPage.js'),
    util = require('util');

function GeneralSettingsPage(overridePath) {
    if (overridePath && overridePath.length > 0) {
        BaseWebPage.call(this, overridePath);
    } else {
        BaseWebPage.call(this, '/manage?tab=configuration');
    }

    this.searchNotConfiguredMessage = element(by.id('sc-search-not-configured-message'));
    this.configuredSearchEndpoint = element(by.id('sc-configured-search-endpoint'));
    this.searchEndpointIsReachable = element(by.id('sc-search-endpoint-is-reachable'));
    this.searchEndpointIsNotReachable = element(by.id('sc-search-endpoint-is-not-reachable'));
}

util.inherits(GeneralSettingsPage, BaseWebPage);

GeneralSettingsPage.prototype.assertSearchEndpointConfiguredAndReachable = function () {
    expect(this.searchNotConfiguredMessage.isDisplayed()).toBeFalsy();
    expect(this.configuredSearchEndpoint.getText()).toBe('localhost:9300');
    expect(this.searchEndpointIsNotReachable.isDisplayed()).toBeFalsy();
    expect(this.searchEndpointIsReachable.isDisplayed()).toBeTruthy();
};

module.exports = GeneralSettingsPage;
