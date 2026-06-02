package com.sakurajima.galsearch.scanner;

import com.sakurajima.galsearch.model.EngineType;

public class ScanResult {
    public String title;
    public String uri;
    public EngineType engine;
    public int confidence;
    public String launchTarget;
    public String coverUri;

    public ScanResult(String title, String uri, EngineType engine, int confidence) {
        this(title, uri, engine, confidence, "", "");
    }

    public ScanResult(String title, String uri, EngineType engine, int confidence, String launchTarget) {
        this(title, uri, engine, confidence, launchTarget, "");
    }

    public ScanResult(String title, String uri, EngineType engine, int confidence, String launchTarget, String coverUri) {
        this.title = title;
        this.uri = uri;
        this.engine = engine;
        this.confidence = confidence;
        this.launchTarget = launchTarget == null ? "" : launchTarget;
        this.coverUri = coverUri == null ? "" : coverUri;
    }
}