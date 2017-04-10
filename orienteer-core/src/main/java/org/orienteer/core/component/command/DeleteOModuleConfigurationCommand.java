package org.orienteer.core.component.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.orienteer.core.boot.loader.util.OrienteerClassLoaderUtil;
import org.orienteer.core.boot.loader.util.artifact.OArtifact;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.widget.loader.IOArtifactsUpdater;

import java.util.List;

/**
 * @author Vitaliy Gonchar
 * Delete Orienteer module
 */
public class DeleteOModuleConfigurationCommand extends AbstractDeleteCommand<OArtifact> {

    private final IOArtifactsUpdater subject;

    public DeleteOModuleConfigurationCommand(OrienteerDataTable<OArtifact, ?> table, IOArtifactsUpdater subject) {
        super(table);
        this.subject = subject;
    }

    @Override
    protected void performMultiAction(AjaxRequestTarget target, List<OArtifact> objects) {
        super.performMultiAction(target, objects);
        subject.notifyAboutNewModules();
    }

    @Override
    protected void perfromSingleAction(AjaxRequestTarget target, final OArtifact module) {
        OrienteerClassLoaderUtil.deleteOModuleConfigurationArtifactFile(module);
        OrienteerClassLoaderUtil.deleteOModuleConfigurationFromMetadata(module);
    }
}
