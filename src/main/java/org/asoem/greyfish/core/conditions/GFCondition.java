package org.asoem.greyfish.core.conditions;


import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.utils.ConfigurableObject;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public interface GFCondition extends GFComponent, ConfigurableObject {

	public boolean evaluate(ActionContext context);
	public boolean isLeafCondition();
	public boolean isRootCondition();
	
	public List<GFCondition> getChildConditions();
	public GFCondition getParentCondition();
	public GFCondition getRoot();
	
	public void setParent(GFCondition parent);
	public boolean add(GFCondition condition);
	public boolean remove(GFCondition condition);
}
