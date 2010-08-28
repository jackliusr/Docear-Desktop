package org.freeplane.plugin.script;

import groovy.lang.Binding;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.plugin.script.proxy.Proxy.NodeRO;

public abstract class FreeplaneScriptBaseClass extends Script {
	private final Pattern nodeIdPattern = Pattern.compile("ID_\\d+");
	private final MetaClass nodeMetaClass;
	private Map<Object, Object> boundVariables;
	private NodeRO node;

	
    public FreeplaneScriptBaseClass() {
	    super();
	    nodeMetaClass = InvokerHelper.getMetaClass(NodeRO.class);
	    initBinding();
    }

    @SuppressWarnings("unchecked")
	public void initBinding() {
	    boundVariables = super.getBinding().getVariables();
	    node = (NodeRO) boundVariables.get("node");
    }

	@Override
    public void setBinding(Binding binding) {
	    super.setBinding(binding);
	    initBinding();
    }

	/** <ul>
	 * <li> translate raw node ids to nodes.
	 * <li> "imports" node's methods into the script's namespace
	 * </ul> */
	public Object getProperty(String property) {
		if (nodeIdPattern.matcher(property).matches()) {
			return N(property);
		}
		else {
			final Object boundValue = boundVariables.get(property);
			if (boundValue != null) {
				return boundValue;
			}
			else {
				try {
					return nodeMetaClass.getProperty(node, property);
				}
				catch (MissingMethodException e) {
					return super.getProperty(property);
				}
			}
		}
	}

	/**
     * extends super class version by node instance methods.
     *
     * @param methodName method to call
     * @param args arguments to pass to the method
     * @return value
     */
    public Object invokeMethod(String methodName, Object args) {
        try {
            return super.invokeMethod(methodName, args);
        }
        catch (MissingMethodException mme) {
        	return nodeMetaClass.invokeMethod(node, methodName, args);
        }
    }

	/** Shortcut for node.map.node(id) - necessary for ids to other maps. */
	public NodeRO N(String id) {
		final NodeRO node = (NodeRO) getBinding().getVariable("node");
		return node.getMap().node(id);
	}

	/** Shortcut for node.map.node(id).text. */
	public String T(String id) {
		final NodeRO n = N(id);
		return n == null ? null : n.getText();
	}

	/** Shortcut for node.map.node(id).value. */
	public Object V(String id) {
		final NodeRO n = N(id);
		return n == null ? null : n.getValue();
	}
}