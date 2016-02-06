package com.gneoxsolutions.betamax.junit;

import com.gneoxsolutions.betamax.Configuration;
import com.google.common.io.Files;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import spock.lang.Issue;

import java.io.File;

@Issue("https://github.com/robfletcher/betamax/issues/36")
@CompileStatic
public class TapeNameTest {

    static final File TAPE_ROOT = Files.createTempDir();
    Configuration configuration = Configuration.builder().tapeRoot(TAPE_ROOT).build();
    @Rule public RecorderRule recorder = new RecorderRule(configuration);

    @AfterClass
    public static void deleteTempDir() {
        ResourceGroovyMethods.deleteDir(TAPE_ROOT);
    }

    @Test
    @Betamax(tape = "explicit name")
    public void tapeCanBeNamedExplicitly() {
        assert recorder.getTape().getName().equals("explicit name");
    }

    @Test
    @Betamax
    public void tapeNameDefaultsToTestName() {
        assert recorder.getTape().getName().equals("tape name defaults to test name");
    }

}
