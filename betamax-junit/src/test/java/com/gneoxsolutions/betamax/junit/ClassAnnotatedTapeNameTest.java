package com.gneoxsolutions.betamax.junit;

import com.gneoxsolutions.betamax.Configuration;
import com.google.common.io.Files;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import spock.lang.Issue;

import java.io.File;

@Issue("https://github.com/robfletcher/betamax/issues/36")
@Betamax
@CompileStatic
public class ClassAnnotatedTapeNameTest {

    static final File TAPE_ROOT = Files.createTempDir();
    static Configuration configuration = Configuration.builder().tapeRoot(TAPE_ROOT).build();
    @ClassRule public static RecorderRule recorder = new RecorderRule(configuration);

    @AfterClass
    public static void deleteTempDir() {
        ResourceGroovyMethods.deleteDir(TAPE_ROOT);
    }

    @Test
    public void tapeNameDefaultsToClassName() {
        assert recorder.getTape().getName().equals("class annotated tape name test");
    }

}
