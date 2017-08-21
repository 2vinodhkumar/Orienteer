package org.orienteer.architect.component.widget;

import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.orienteer.architect.OArchitectModule;
import org.orienteer.architect.component.behavior.*;
import org.orienteer.architect.component.panel.SchemaOClassesPanel;
import org.orienteer.architect.component.panel.command.OArchitectFullScreenCommand;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;
import ru.ydn.wicket.wicketorientdb.security.RequiredOrientResource;

import java.util.Map;
import java.util.Set;

/**
 * Editor widget for OrientDB Schema
 */
@Widget(id="architect-editor", domain = "document", selector = OArchitectModule.ODATA_MODEL_OCLASS, autoEnable = true, order=10)
@RequiredOrientResource(value = OSecurityHelper.CLUSTER, permissions = OrientPermission.READ)
public class OArchitectEditorWidget extends AbstractWidget<ODocument> {

    private static final Logger LOG = LoggerFactory.getLogger(OArchitectEditorWidget.class);

    private static final JavaScriptResourceReference MXGRAPH_JS = new WebjarsJavaScriptResourceReference("mxgraph/current/javascript/mxClient.min.js");
    private static final CssResourceReference MXGRAPH_CSS    = new WebjarsCssResourceReference("mxgraph/current/javascript/src/css/common.css");
    private static final CssResourceReference OARCHITECT_CSS = new CssResourceReference(OArchitectEditorWidget.class, "css/architect.css");

    private WebMarkupContainer container;
    private WebMarkupContainer editor;
    private WebMarkupContainer toolbar;
    private WebMarkupContainer sidebar;
    private WebMarkupContainer outline;

    public OArchitectEditorWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        container = newContainer("container");
        container.add(editor = newContainer("editor"));
        container.add(toolbar = newContainer("toolbar"));
        container.add(sidebar = newContainer("sidebar"));
        container.add(outline = newContainer("outline"));
        SchemaOClassesPanel panel = new SchemaOClassesPanel("listClasses", "; app.executeCallback('%s');");
        container.add(panel);
        add(container);
        add(new GetNewChangesBehavior());
        add(new ManageEditorConfigBehavior(getModel()));
        add(new ApplyEditorChangesBehavior());
        add(new GetOClassesBehavior(panel));
        add(new ExistsOClassBehavior());
    }

    private WebMarkupContainer newContainer(String id) {
        WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        return container;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(OARCHITECT_CSS));
        response.render(CssReferenceHeaderItem.forReference(MXGRAPH_CSS));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/OArchitectApplication.js")));
        response.render(JavaScriptHeaderItem.forScript(
                String.format("; initMxGraph('%s');", "en"), null));
        response.render(JavaScriptHeaderItem.forReference(MXGRAPH_JS));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectEditor.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectBar.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectOType.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectMessage.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectPopupMenu.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectModalWindow.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OArchitectValueContainer.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OClassEditModalWindow.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/OPropertyEditModalWindow.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/behavior/OArchitectAction.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/config/GraphConfig.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/config/GraphConnectionConfig.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/component/config/GraphStyleConfig.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectOClass.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectOProperty.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectOClassConfigurator.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectUtil.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectConstants.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class, "js/util/OArchitectConnector.js")));
        String locale = getOArchitectEditorLocale();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(OArchitectEditorWidget.class,
                String.format("js/util/locale/architect_%s.js", locale))));

        PackageResourceReference configXml = new PackageResourceReference(OArchitectEditorWidget.class, "js/OArchitectApplication.js");
        String configUrl = urlFor(configXml, null).toString();
        String baseUrl = configUrl.substring(0, configUrl.indexOf("js/OArchitectApplication"));
        TextTemplate configTemplate = new PackageTextTemplate(OArchitectEditorWidget.class, "config.tmpl.xml");
        Map<String, Object> params = CommonUtils.toMap("basePath", baseUrl);
        String config = configTemplate.asString(params);
        boolean canUpdate = canUserUpdateEditor();
        response.render(OnLoadHeaderItem.forScript(String.format("init('%s', %s, %s, '%s', '%s', '%s', '%s', '%s', '%s', %s);",
											        		baseUrl,
											        		CommonUtils.escapeAndWrapAsJavaScriptString(config),
											                locale,
											                this.getMarkupId(),
											                container.getMarkupId(),
											                editor.getMarkupId(),
											                sidebar.getMarkupId(),
											                toolbar.getMarkupId(),
                                                            outline.getMarkupId(),
                                                            Boolean.toString(canUpdate))));
    }

    private String getOArchitectEditorLocale() {
        String locale = getLocale().getLanguage();
        if (locale.equals("en") || locale.equals("ru") || locale.equals("uk"))
            return locale;
        return "en";
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.edit);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.architect.editor.title");
    }
    
    @Override
    protected String getWidgetStyleClass() {
    	return "strict";
    }

    private boolean canUserUpdateEditor() {
        boolean canUpdate = false;
        Set<? extends OSecurityRole> roles = OrienteerWebSession.get().getUser().getRoles();

        for (OSecurityRole role : roles) {
            canUpdate = role.allow(ORule.ResourceGeneric.CLUSTER, "internal", OrientPermission.UPDATE.getPermissionFlag());
            if (canUpdate) canUpdate = role.allow(ORule.ResourceGeneric.CLUSTER, "internal", OrientPermission.CREATE.getPermissionFlag());
            if (canUpdate) canUpdate = role.allow(ORule.ResourceGeneric.SCHEMA, "", OrientPermission.CREATE.getPermissionFlag());
            if (canUpdate) canUpdate = role.allow(ORule.ResourceGeneric.SCHEMA, "", OrientPermission.UPDATE.getPermissionFlag());
            if (canUpdate)
                break;
        }
        return canUpdate;
    }

    @Override
    protected Command<ODocument> newFullScreenCommand(String id) {
        final OArchitectFullScreenCommand<ODocument> command = new OArchitectFullScreenCommand<>(id);
        add(new AbstractDefaultAjaxBehavior() {

            private static final String FULLSCREEN_VAR = "fullscreen";

            @Override
            protected void respond(AjaxRequestTarget target) {
                IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
                command.setExpanded(target, params.getParameterValue(FULLSCREEN_VAR).toBoolean());
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                response.render(OnLoadHeaderItem.forScript(
                        String.format("; app.setSwitchFullScreenMode('%s');", getCallbackUrl())));
            }
        });
        return command;
    }
}
