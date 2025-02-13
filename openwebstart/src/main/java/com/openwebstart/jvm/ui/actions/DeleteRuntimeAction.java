package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;

public class DeleteRuntimeAction extends BasicAction<LocalJavaRuntime> {

    public DeleteRuntimeAction() {
        super("delete JVM", "delete the JVM");
    }

    @Override
    public void call(final LocalJavaRuntime item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                LocalRuntimeManager.getInstance().delete(item);
            } catch (final Exception e) {
                SwingUtilities.invokeLater(() -> new ErrorDialog("Can not delete local folder", e).showAndWait());
            }
        });
    }
}
