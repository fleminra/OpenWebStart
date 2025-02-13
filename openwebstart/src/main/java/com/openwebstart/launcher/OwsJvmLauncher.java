package com.openwebstart.launcher;

import com.openwebstart.jvm.util.JavaExecutableFinder;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.Boot;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;

/**
 * ...
 */
class OwsJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(OwsJvmLauncher.class);

    private final JavaHomeProvider javaHomeProvider;

    OwsJvmLauncher(JavaHomeProvider javaHomeProvider) {
        this.javaHomeProvider = javaHomeProvider;
    }

    @Override
    public void launchExternal(JNLPFile jnlpFile, List<String> args) throws Exception {

        final Path javaHome = getJavaHome(jnlpFile);

        LOG.info("using java runtime at '{}' for launching managed application", javaHome);

        final String pathToJavaBinary = JavaExecutableFinder.findJavaExecutable(javaHome);
        final String pathToJar = getPathToOpenWebStartJar();
        launchExternal(pathToJavaBinary, pathToJar, jnlpFile.getNewVMArgs(), args);
    }

    private Path getJavaHome(JNLPFile jnlpFile) {
        for (JREDesc jre : jnlpFile.getResources().getJREs()) {
            LOG.debug("searching for JRE with version string '{}'", jre.getVersion());

            final Path javaHome = javaHomeProvider.getJavaHome(jre.getVersion(), jre.getLocation());
            if (javaHome != null) {
                return javaHome;
            }
        }

        throw new IllegalStateException("could not find any suitable runtime");
    }

    /**
     * @param pathToJavaBinary path to the java binary of the JRE in which to start OWS
     * @param pathToJar        path to the openwebstart.jar included in OWS
     * @param vmArgs           the arguments to pass to the jvm
     * @param javawsArgs       the arguments to pass to javaws (aka IcedTea-Web)
     */
    private void launchExternal(String pathToJavaBinary, String pathToJar, List<String> vmArgs, List<String> javawsArgs) throws Exception {
        final List<String> commands = new LinkedList<>();

        commands.add(quoteIfRequired(pathToJavaBinary));
        commands.add("-cp");
        commands.add(quoteIfRequired(pathToJar));

        commands.addAll(vmArgs);
        commands.add(Boot.class.getName());
        commands.addAll(javawsArgs);

        LOG.info("About to launch external with commands: '{}'", commands.toString());

        final Process p = new ProcessBuilder()
                .command(commands)
                .inheritIO()
                .start();

        ProcessUtils.waitForSafely(p);
    }

    private static String getPathToOpenWebStartJar() {
        final String classPath = System.getProperty("java.class.path");
        final String pathSeparator = System.getProperty("path.separator");
        final String javaHome = System.getProperty("java.home");
        final String[] classpathElements = classPath.split(Pattern.quote(pathSeparator));

        final List<String> jarCandidates = Arrays.stream(classpathElements)
                .filter((e) -> e.endsWith("openwebstart.jar"))
                .filter((e) -> !e.startsWith(javaHome))
                .collect(Collectors.toList());

        if (jarCandidates.size() == 1) {
            return jarCandidates.get(0);
        } else if (jarCandidates.size() > 1) {
            throw new IllegalStateException("multiple openwebstart jars found in classpath");
        } else {
            throw new IllegalStateException("openwebstart jar not found in classpath");
        }
    }

}
