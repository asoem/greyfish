package org.asoem.sico.core.acl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javolution.context.ObjectFactory;
import javolution.lang.Reusable;
import javolution.util.FastList;

import org.asoem.sico.core.individual.Individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public class ACLMessage implements Reusable {
	
	private final static ObjectFactory<ACLMessage> FACTORY = new ObjectFactory<ACLMessage>() {
		
		@Override
		protected ACLMessage create() {
			return new ACLMessage();
		}
	};
	
	private ACLPerformative performative;

	private Individual source;

	/** These constants represent the expected size of the 2 array lists
	 * used by this class **/
	private static final int RECEIVERS_EXPECTED_SIZE = 1;

	private List<Individual> dests = new FastList<Individual>(RECEIVERS_EXPECTED_SIZE);
	private List<Individual> reply_to = null;

	// At a given time or content or byteSequenceContent are != null,
	// it is not allowed that both are != null
	private StringBuffer content = null;
	private byte[] byteSequenceContent = null;
	private Object referenceContent = null;

	private String reply_with = null;

	private String in_reply_to = null;

	private String encoding = null;

	private String language = null;

	private String ontology = null;

	private String protocol = null;

	private static int progressiveId = 0;
	private int conversation_id = 0;

	private ACLMessage() {
		this.performative = ACLPerformative.NOT_UNDERSTOOD;
		this.conversation_id = ++progressiveId;
	}

	public static ACLMessage newInstance() {
		return FACTORY.object();
	}
	
	public static void recycle(ACLMessage message) {
		FACTORY.recycle(message);
	}

	/**
	 Writes the <code>:sender</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param source The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getSender()
	 */
	public void setSender(Individual s) {
		source = checkNotNull(s);
	}

	/**
	 * set the performative of this ACL message object to the passed constant.
	 * Remind to 
	 * use the set of constants (i.e. <code> INFORM, REQUEST, ... </code>)
	 * defined in this class
	 */
	public void setPerformative(ACLPerformative perf) {
		performative = perf;
	}

	/**
	 * Writes the <code>:content</code> slot. <em><b>Warning:</b> no
	 * checks are made to validate the slot value.</em> <p>
	 * <p>Notice that, in general, setting a String content and getting
	 * back a byte sequence content - or viceversa - does not return
	 * the same value, i.e. the following relation does not hold
	 * <code>
	 * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
	 * is equal to getByteSequenceContent()
	 * </code>
	 * @param content The new value for the slot.
	 * @see jade.lang.acl.ACLMessage#getContent()
	 * @see jade.lang.acl.ACLMessage#setByteSequenceContent(byte[])
	 * @see jade.lang.acl.ACLMessage#setContentObject(Serializable s)
	 */
	public void setContent(String content) {
		byteSequenceContent = null;
		referenceContent = null;
		if (content != null) {
			this.content = new StringBuffer(content);
		}
		else {
			this.content = null;
		}
	}

	/**
	 * Writes the <code>:content</code> slot. <em><b>Warning:</b> no
	 * checks are made to validate the slot value.</em> <p>
	 * <p>Notice that, in general, setting a String content and getting
	 * back a byte sequence content - or viceversa - does not return
	 * the same value, i.e. the following relation does not hold
	 * <code>
	 * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
	 * is equal to getByteSequenceContent()
	 * </code>
	 * @param byteSequenceContent The new value for the slot.
	 * @see jade.lang.acl.ACLMessage#setContent(String s)
	 * @see jade.lang.acl.ACLMessage#getByteSequenceContent()
	 * @see jade.lang.acl.ACLMessage#setContentObject(Serializable s)
	 */
	public void setByteSequenceContent(byte[] byteSequenceContent) {
		content = null;
		referenceContent = null;
		this.byteSequenceContent = byteSequenceContent;
	}

	/**
	 * This method sets the content of this ACLMessage to a Java object.
	 * It is not FIPA compliant so its usage is not encouraged.
	 * For example:<br>
	 * <PRE>
	 * ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	 * Date d = new Date(); 
	 * try{
	 *  msg.setContentObject(d);
	 * }catch(IOException e){}
	 * </PRE>
	 *
	 * @param s the object that will be used to set the content of the ACLMessage. 
	 * @exception IOException if an I/O error occurs.
	 */
	public void setContentObject(java.io.Serializable s) throws IOException
	{
		ByteArrayOutputStream c = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(c);
		oos.writeObject(s);
		oos.flush();
		setByteSequenceContent(c.toByteArray());
	}


	/**
	 * This method returns the content of this ACLMessage when they have
	 * been written via the method <code>setContentObject</code>.
	 * It is not FIPA compliant so its usage is not encouraged.
	 * For example to read Java objects from the content 
	 * <PRE>
	 * ACLMessage msg = blockingReceive();
	 * try{
	 *  Date d = (Date)msg.getContentObject();
	 * }catch(UnreadableException e){}
	 * </PRE>
	 * 
	 * @return the object read from the content of this ACLMessage
	 * @exception UnreadableException when an error occurs during the decoding.
	 */
	public java.io.Serializable getContentObject() throws UnreadableException
	{

		try{
			byte[] data = getByteSequenceContent();
			if (data == null)
				return null;
			ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
			java.io.Serializable s = (java.io.Serializable)oin.readObject();
			return s;
		}
		catch (java.lang.Error e) {
			throw new UnreadableException(e.getMessage());
		}
		catch (IOException e1) {
			throw new UnreadableException(e1.getMessage());
		}
		catch(ClassNotFoundException e2) {
			throw new UnreadableException(e2.getMessage());
		}

	}

	public void setReferenceContent(Object o) {
		referenceContent = o;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getReferenceContent(Class<T> clazz) throws IllegalArgumentException {
		Preconditions.checkArgument(clazz != null
				&& clazz.isInstance(referenceContent));
		return (T) referenceContent;
	}
	
	/**
	 Writes the <code>:reply-with</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param reply The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getReplyWith()
	 */
	public void setReplyWith(String reply) {
		reply_with = reply; 
	}

	/**
	 Writes the <code>:in-reply-to</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param reply The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getInReplyTo()
	 */
	public void setInReplyTo(String reply) {
		in_reply_to = reply;
	}

	/**
	 Writes the <code>:encoding</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param str The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getEncoding()
	 */
	public void setEncoding(String str) {
		encoding = str;
	}

	/**
	 Writes the <code>:language</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param str The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getLanguage()
	 */
	public void setLanguage(String str) {
		language = str;
	}

	/**
	 Writes the <code>:ontology</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param str The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getOntology()
	 */
	public void setOntology(String str) {
		ontology = str;
	}

	/**
	 Writes the <code>:protocol</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param str The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getProtocol()
	 */
	public void setProtocol( String str ) {
		protocol = str;
	}

	/**
	 Writes the <code>:conversation-id</code> slot. <em><b>Warning:</b> no
	 checks are made to validate the slot value.</em>
	 @param str The new value for the slot.
	 @see jade.lang.acl.ACLMessage#getConversationId()
	 */
	public void setConversationId( int str ) {
		conversation_id = str;
	}



	/**
	 Reads <code>:receiver</code> slot.
	 @return An <code>Iterator</code> containing the Agent IDs of the
	 receiver agents for this message.
	 */
	public Collection<Individual> getAllReceiver() {
		return dests;
	}

	/**
	 Reads <code>:reply_to</code> slot.
	 @return An <code>Iterator</code> containing the Agent IDs of the
	 reply_to agents for this message.
	 */
	public Iterator<Individual> getAllReplyTo() {
		if (reply_to == null) {
			return Iterators.emptyIterator();
		}
		else {
			return reply_to.iterator();
		}
	}

	/**
	 Reads <code>:sender</code> slot.
	 @return The value of <code>:sender</code>slot.
	 @see jade.lang.acl.ACLMessage#setSender(AID).
	 */
	public Individual getSender() {
		return source;
	}

	/**
	 * return the integer representing the performative of this object
	 * @return an integer representing the performative of this object
	 */
	public ACLPerformative getPerformative() {
		return performative;
	}

	/**
	 * This method allows to check if the content of this ACLMessage
	 * is a byteSequence or a String
	 * @return true if it is a byteSequence, false if it is a String
	 */
	public boolean hasByteSequenceContent(){
		return (byteSequenceContent != null);
	}

	public boolean hasReferenceContent() {
		return referenceContent != null;
	}
	
	/**
	 * Reads <code>:content</code> slot. <p>
	 * <p>Notice that, in general, setting a String content and getting
	 * back a byte sequence content - or viceversa - does not return
	 * the same value, i.e. the following relation does not hold
	 * <code>
	 * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
	 * is equal to getByteSequenceContent()
	 * </code>
	 * @return The value of <code>:content</code> slot.
	 * @see jade.lang.acl.ACLMessage#setContent(String)
	 * @see jade.lang.acl.ACLMessage#getByteSequenceContent()
	 * @see jade.lang.acl.ACLMessage#getContentObject()
	 */
	public String getContent() {
		if(content != null)
			return new String(content);
		else if (byteSequenceContent != null)
			return new String(byteSequenceContent);
		return null;
	}

	/**
	 * Reads <code>:content</code> slot. <p>
	 * <p>Notice that, in general, setting a String content and getting
	 * back a byte sequence content - or viceversa - does not return
	 * the same value, i.e. the following relation does not hold
	 * <code>
	 * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
	 * is equal to getByteSequenceContent()
	 * </code>
	 * @return The value of <code>:content</code> slot.
	 * @see jade.lang.acl.ACLMessage#getContent()
	 * @see jade.lang.acl.ACLMessage#setByteSequenceContent(byte[])
	 * @see jade.lang.acl.ACLMessage#getContentObject()
	 */
	public byte[] getByteSequenceContent() {
		if (content != null) 
			return content.toString().getBytes();
		else if (byteSequenceContent != null)
			return byteSequenceContent;
		return null;
	}

	/**
	 Reads <code>:reply-with</code> slot.
	 @return The value of <code>:reply-with</code>slot.
	 @see jade.lang.acl.ACLMessage#setReplyWith(String).
	 */
	public String getReplyWith() {
		return reply_with;
	}

	/**
	 Reads <code>:reply-to</code> slot.
	 @return The value of <code>:reply-to</code>slot.
	 @see jade.lang.acl.ACLMessage#setInReplyTo(String).
	 */
	public String getInReplyTo() {
		return in_reply_to;
	}



	/**
	 Reads <code>:encoding</code> slot.
	 @return The value of <code>:encoding</code>slot.
	 @see jade.lang.acl.ACLMessage#setEncoding(String).
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 Reads <code>:language</code> slot.
	 @return The value of <code>:language</code>slot.
	 @see jade.lang.acl.ACLMessage#setLanguage(String).
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 Reads <code>:ontology</code> slot.
	 @return The value of <code>:ontology</code>slot.
	 @see jade.lang.acl.ACLMessage#setOntology(String).
	 */
	public String getOntology() {
		return ontology;
	}

	/**
	 Reads <code>:protocol</code> slot.
	 @return The value of <code>:protocol</code>slot.
	 @see jade.lang.acl.ACLMessage#setProtocol(String).
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 Reads <code>:conversation-id</code> slot.
	 @return The value of <code>:conversation-id</code>slot.
	 @see jade.lang.acl.ACLMessage#setConversationId(String).
	 */
	public int getConversationId() {
		return conversation_id;
	}

	/**
	 Adds a value to <code>:receiver</code> slot. <em><b>Warning:</b>
	 no checks are made to validate the slot value.</em>
	 @param r The value to add to the slot value set.
	 */
	public void addReceiver(Individual r) {
		dests.add(checkNotNull(r));
	}
	
	public void addReceivers(Collection<Individual> receivers) {
		dests.addAll(checkNotNull(receivers));
	}
	
	public void send(final ACLMessageTransmitter transmitter) {
		transmitter.deliverMessage(this);
	}

	/**
	 * create a new ACLMessage that is a reply to this message.
	 * In particular, it sets the following parameters of the new message:
	 * receiver, language, ontology, protocol, conversation-id,
	 * in-reply-to, reply-with.
	 * The programmer needs to set the communicative-act and the content.
	 * Of course, if he wishes to do that, he can reset any of the fields.
	 * @return the ACLMessage to send as a reply
	 */
	public ACLMessage createReply() {
		ACLMessage m = newInstance();
		
		m.setPerformative(getPerformative());	
		Iterator<Individual> it = getAllReplyTo(); 
		while (it.hasNext())
			m.addReceiver(it.next());
		if ((reply_to == null) || reply_to.isEmpty())
			m.addReceiver(getSender());
		m.setLanguage(getLanguage());
		m.setOntology(getOntology());
		m.setProtocol(getProtocol());
		m.setInReplyTo(getReplyWith());
		m.generateReplyWith();
		m.setConversationId(getConversationId());
		
		return m;
	}
	
	public void generateReplyWith() {
		if (source != null)
			setReplyWith(source.getName() + java.lang.System.currentTimeMillis()); 
		else
			setReplyWith("X"+java.lang.System.currentTimeMillis());
	}

	public boolean matches(MessageTemplate performative) {
		return performative.apply(this);
	}

	public String toString(){
		StringBuffer str = new StringBuffer("(");
		str.append(getPerformative() + "\n");
		
		Individual sender = getSender();
		if (sender != null) 
			str.append(":sender" + " "+ sender.toString()+"\n");
		Iterator<Individual> it = getAllReceiver().iterator();
		if (it.hasNext()) {
			str.append(":receiver" + " (set ");
			while(it.hasNext()) 
				str.append(it.next().toString()+" ");
			str.append(")\n");
		}
		it = getAllReplyTo();
		if (it.hasNext()) {
			str.append(":reply-to" + " (set \n");
			while(it.hasNext()) 
				str.append(it.next().toString()+" ");
			str.append(")\n");
		}
		if (hasByteSequenceContent()) {
			str.append(":content" + " <BINARY> \n");
		} else {
			String content = getContent();
			if (content != null) {
				content = content.trim();
				if (content.length() > 0)
					str.append(":content" + " \"" + content + "\" \n");
			}
		}
		
		// Description of Content
		str.append(":encoding " + getEncoding() + "\n");
		str.append(":language " + getLanguage() + "\n");
		str.append(":ontology " + getOntology() + "\n");
		
		// Control of Conversation
		str.append(":protocol " + getProtocol() + "\n");
		str.append(":conversation-id " + getConversationId() + "\n");
		str.append(":reply-with " + getReplyWith() + "\n");
		str.append(":in-reply-to " + getInReplyTo() + "\n");
		
		str.append(")");

		return str.toString();
	}

	@Override
	public void reset() {
		this.performative = null;
		this.byteSequenceContent = null;
		this.content = null;
		this.conversation_id = 0;
		this.dests.clear();
		this.encoding = null;
		this.in_reply_to = null;
		this.language = null;
		this.ontology = null;
		this.protocol = null;
		this.referenceContent = null;
		this.reply_to = null;
		this.reply_with = null;
		this.source = null;
	}
	
	public ACLMessage createCopy() {
		ACLMessage ret = newInstance();
		
		ret.performative = this.performative;
		ret.byteSequenceContent = this.byteSequenceContent;
		ret.content = this.content;
		ret.conversation_id = this.conversation_id;
		ret.dests.clear();
		ret.dests.addAll(this.dests);
		ret.encoding = this.encoding;
		ret.in_reply_to = this.in_reply_to;
		ret.language = this.language;
		ret.ontology = this.ontology;
		ret.protocol = this.protocol;
		ret.referenceContent = this.referenceContent;
		ret.reply_to = this.reply_to;
		ret.reply_with = this.reply_with;
		ret.source = this.source;
		
		return ret;
	}
}
