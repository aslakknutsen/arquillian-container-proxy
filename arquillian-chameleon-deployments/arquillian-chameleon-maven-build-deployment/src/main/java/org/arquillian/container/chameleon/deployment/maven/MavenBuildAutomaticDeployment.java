package org.arquillian.container.chameleon.deployment.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import org.arquillian.container.chameleon.deployment.api.AbstractAutomaticDeployment;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.ConfigurationDistributionStage;
import org.jboss.shrinkwrap.resolver.impl.maven.embedded.BuiltProjectImpl;

public class MavenBuildAutomaticDeployment extends AbstractAutomaticDeployment {

    @Override
    protected Archive<?> build(TestClass testClass) {
        if (testClass.isAnnotationPresent(MavenBuild.class)) {
            final MavenBuild mavenBuildDeployment = testClass.getAnnotation(MavenBuild.class);
            return runBuild(mavenBuildDeployment);
        }

        return null;
    }

    private Archive<?> runBuild(MavenBuild conf) {
        final ConfigurationDistributionStage configurationDistributionStage = EmbeddedMaven.forProject(conf.pom())
            .useMaven3Version(conf.version())
            .setGoals(conf.goals())
            .setProfiles(conf.profiles())
            .setOffline(conf.offline())
            .setQuiet()
            .skipTests(true);

        if (isNotEmptyOrNull(conf.localRepositoryDirectory())) {
            configurationDistributionStage.setLocalRepositoryDirectory(new File(conf.localRepositoryDirectory()));
        }

        if (isNotEmptyOrNull(conf.mvnOpts())) {
            configurationDistributionStage.setMavenOpts(conf.mvnOpts());
        }

        final String[] properties = conf.properties();

        if (properties.length % 2 != 0) {
            throw new IllegalArgumentException(String.format(
                "Maven properties must be set in an array of pairs key, value, but in %s properties are odd",
                Arrays.toString(properties)));
        }

        for (int i = 0; i < properties.length; i += 2) {
            configurationDistributionStage.addProperty(properties[i], properties[i + 1]);
        }

        BuiltProject build = configurationDistributionStage
            .ignoreFailure()
            .build();

        if (isModuleSet(conf)) {
            final File projectDirectory = build.getModel().getProjectDirectory();
            final Path normalize = projectDirectory.toPath().normalize();

            final String relativeModuleDirectory = conf.module().replace('/', File.separatorChar);

            build = getSubmodule(build,
                normalize.toString() + File.separator + relativeModuleDirectory + File.separator + "pom.xml");
        }

        return build.getDefaultBuiltArchive();

    }

    private boolean isModuleSet(MavenBuild conf) {
        return !"".equals(conf.module());
    }

    private BuiltProject getSubmodule(BuiltProject builtProject, String pomfile) {
        BuiltProjectImpl submodule = new BuiltProjectImpl(
            pomfile
        );
        submodule.setMavenBuildExitCode(builtProject.getMavenBuildExitCode());
        submodule.setMavenLog(builtProject.getMavenLog());
        return submodule;
    }

    private boolean isNotEmptyOrNull(String value) {
        // When we update Chameleon to Java 8 this can be changed to isEmpty
        return value != null && value.length() > 0;
    }

}
