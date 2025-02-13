package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_HOME;

public class MacRuntimeFinder implements RuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(MacRuntimeFinder.class);

    private static final String MAC_JVM_BASEFOLDER = "/Library/Java/JavaVirtualMachines";

    @Override
    public List<Result<LocalJavaRuntime>> findLocalRuntimes() {
        LOG.debug("Searching for local runtimes");

        final Path systemPath = Paths.get(MAC_JVM_BASEFOLDER);
        final Path sdkmanPath = Paths.get(System.getProperty(USER_HOME) + File.separatorChar + ".sdkman");

        return JdkFinder.findLocalJdks(systemPath, sdkmanPath);
    }

    @Override
    public List<OperationSystem> getSupportedOperationSystems() {
        return Collections.singletonList(OperationSystem.MAC64);
    }
}
