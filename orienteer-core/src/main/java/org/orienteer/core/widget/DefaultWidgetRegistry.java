package org.orienteer.core.widget;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;

import com.google.inject.Singleton;

@Singleton
public class DefaultWidgetRegistry implements IWidgetRegistry {
	
	private List<IWidgetDescription<?>> widgetDescriptions = new ArrayList<IWidgetDescription<?>>();
	
	@Override
	public List<IWidgetDescription<?>> listWidgetDescriptions() {
		return Collections.unmodifiableList(widgetDescriptions);
	}

	@Override
	public IWidgetDescription<?> lookupById(String id) {
		if(id==null) return null;
		for(IWidgetDescription<?> description : widgetDescriptions)
		{
			if(id.equals(description.getId())) return description;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<IWidgetDescription<T>> lookupByType(Class<T> typeClass) {
		List<IWidgetDescription<T>> ret = new ArrayList<IWidgetDescription<T>>();
		for(IWidgetDescription<?> description : widgetDescriptions)
		{
			if(typeClass.equals(description.getType())) ret.add((IWidgetDescription<T>)description);
		}
		return Collections.unmodifiableList(ret);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IWidgetDescription<T> lookupByWidgetClass( Class<? extends AbstractWidget<T>> widgetClass) {
		if(widgetClass==null) return null;
		for(IWidgetDescription<?> description : widgetDescriptions)
		{
			if(widgetClass.equals(description.getWidgetClass())) return (IWidgetDescription<T>)description;
		}
		return null;
	}

	@Override
	public IWidgetRegistry register(IWidgetDescription<?> description) {
		widgetDescriptions.add(description);
		return this;
	}

	@Override
	public <T> IWidgetRegistry register(final Class<? extends AbstractWidget<T>> widgetClass) {
		final Widget widget = widgetClass.getAnnotation(Widget.class);
		if(widget==null) throw new WicketRuntimeException("There is no a @Widget annotation on "+widgetClass.getName());
		return register(new IWidgetDescription<T>() {

			@Override
			public String getId() {
				return widget.id();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<T> getType() {
				return (Class<T>)widget.type();
			}

			@Override
			public Class<? extends AbstractWidget<T>> getWidgetClass() {
				return widgetClass;
			}

			@Override
			public AbstractWidget<T> instanciate(String componentId,
					IModel<T> model) {
				try {
					return getWidgetClass().getConstructor(String.class, IModel.class).newInstance(componentId, model);
				} catch (Exception e) {
					throw new WicketRuntimeException("Can't instanciate widget for descriptor: "+this , e);
				} 
			}

			@Override
			public boolean compatible(T testObject) {
				return getType().isInstance(testObject);
			}
			
			@Override
			public String toString() {
				return widget.toString();
			}
		});
	}

}
