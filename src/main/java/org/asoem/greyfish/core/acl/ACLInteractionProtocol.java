package org.asoem.sico.core.acl;

public enum ACLInteractionProtocol {

	CONTRACT_NET("Contract Net") {

		@Override
		public ACLMessage createMessageStub() {
			final ACLMessage ret = ACLMessage.newInstance();// new ACLMessage(ACLPerformative.CFP);
			ret.setProtocol(getName());
			return ret;
		}
		
	};
	
	private String name;
	
	private ACLInteractionProtocol(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract ACLMessage createMessageStub();
}
