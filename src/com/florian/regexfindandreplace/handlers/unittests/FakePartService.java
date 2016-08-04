package com.florian.regexfindandreplace.handlers.unittests;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class FakePartService implements IPartService {

	private List<IPartListener2> partListeners = new LinkedList<IPartListener2>();

	private IWorkbenchPartReference activePart;
	
	public void setActivePart(IWorkbenchPartReference activePart) {
		if(this.activePart != activePart)
		{
			this.activePart = activePart;
			firePartActivated();
		}
	}
	
	protected void firePartActivated()
	{
		for(IPartListener2 partListener : partListeners )
		{
			partListener.partActivated(activePart);
		}
	}

	

	@Override
	public void addPartListener(IPartListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPartListener(IPartListener2 listener) {
		partListeners.add(listener);
		
	}

	@Override
	public IWorkbenchPart getActivePart() {
		// TODO Auto-generated method stub
		return activePart.getPart(true);
	}

	@Override
	public IWorkbenchPartReference getActivePartReference() {
		// TODO Auto-generated method stub
		return activePart;
	}

	@Override
	public void removePartListener(IPartListener listener) {
		
		
	}

	@Override
	public void removePartListener(IPartListener2 listener) {
		partListeners.remove(listener);
		
	}}
