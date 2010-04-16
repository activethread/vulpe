package org.vulpe.view.struts.result;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.vulpe.controller.struts.action.VulpeBaseAction;


import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

@SuppressWarnings("serial")
public class ParametersServletActionRedirectResult extends
		ServletActionRedirectResult {
	public ParametersServletActionRedirectResult() {
		super();
		sendParameters = false;
		sendParamsInSession = false;
		saveParamsInSession = false;
//		prohibitedResultParam = Arrays.asList(new String[] { "sendParameters",
//				"sendParamsInSession", "saveParamsInSession", DEFAULT_PARAM,
//				"namespace", "method", "encode", "parse", "location",
//				"prependServletContext" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(final ActionInvocation invocation) throws Exception {
		String name = ActionContext.getContext().getName().substring(
				0,
				StringUtils.lastIndexOf(ActionContext.getContext().getName(),
						'.'));
		if (saveParamsInSession) {
			invocation.getInvocationContext().getSession().put(name,
					invocation.getInvocationContext().getParameters());
		} else if (sendParamsInSession) {
			if (invocation.getAction() instanceof VulpeBaseAction) {
				final VulpeBaseAction action = (VulpeBaseAction) invocation.getAction();
				name = action.getActionConfig().getOwnerAction();
			}
			final Map params = (Map) invocation.getInvocationContext().getSession()
					.get(name);
			if (params != null) {
				for (Object key : params.keySet()) {
					final String[] value = (String[]) params.get(key);
					addParameter((String) key, value[0]);
				}
				invocation.getInvocationContext().getSession().remove(name);
			}
		}

		if (sendParameters) {
			for (Object key : invocation.getInvocationContext().getParameters()
					.keySet()) {
				final String[] val = (String[]) invocation.getInvocationContext()
						.getParameters().get(key);
				addParameter((String) key, val[0]);
			}
		}

		super.execute(invocation);
	}

	private boolean sendParameters;
	private boolean sendParamsInSession;
	private boolean saveParamsInSession;

	public boolean isSendParameters() {
		return sendParameters;
	}

	public void setSendParameters(final boolean sendParameters) {
		this.sendParameters = sendParameters;
	}

	public boolean isSendParamsInSession() {
		return sendParamsInSession;
	}

	public void setSendParamsInSession(final boolean sendParamsInSession) {
		this.sendParamsInSession = sendParamsInSession;
	}

	public boolean isSaveParamsInSession() {
		return saveParamsInSession;
	}

	public void setSaveParamsInSession(final boolean saveParamsInSession) {
		this.saveParamsInSession = saveParamsInSession;
	}
}