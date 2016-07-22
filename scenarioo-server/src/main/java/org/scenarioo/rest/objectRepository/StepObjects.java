package org.scenarioo.rest.objectRepository;

public class StepObjects {
    private final String stepDetailUrl;
    private final String screenShotUrl;

    public StepObjects(String stepDetailUrl, String screenShotUrl) {
        this.stepDetailUrl = stepDetailUrl;
        this.screenShotUrl = screenShotUrl;
    }

    public String getStepDetailUrl() {
        return stepDetailUrl;
    }

    public String getScreenShotUrl() {
        return screenShotUrl;
    }
}

