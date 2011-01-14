package org.asoem.greyfish.core.acl;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import javolution.context.ObjectFactory;
import javolution.lang.Reusable;
import javolution.util.FastList;
import org.asoem.greyfish.core.individual.Individual;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ACLMessage implements Reusable {
	
	private final static ObjectFactory<ACLMessage> FACTORY = new ObjectFactory<ACLMessage>() {
		
		@Override
		protected ACLMessage create() {
			return new ACLMessage();
		}
	};
	
	private ACLPerformative performative = ACLPerformative.NOT_UNDERSTOOD;

	private Individual source;

	/** These constants represent the expected size of the 2 array lists
	 * used by this class **/
	private static final int RECEIVERS_EXPECTED_SIZE = 1;

	private List<Individual> dests = new FastList<Individual>(RECEIVERS_EXPECTED_SIZE);
	private List<Individual> reply_to = null;

    public enum ContentType {
        NULL,
        STRING,
        BYTE_ARRAY,
        OTHER
    }

    public ContentType getContentType() {
        return contentType;
    }

    private ContentType contentType = ContentType.BYTE_ARRAY;

    private final static Object NULL_CONTENT = new Object();
    private Object content = NULL_CONTENT;

	private String reply_with = null;

	private String in_reply_to = null;

	private String encoding = null;

	private String language = null;

	private String ontology = null;

	private String protocol = null;

	private static int progressiveId = 0;
	private int conversation_id = 0;

	private ACLMessage() {
	}

	public static ACLMessage newInstance() {
		return FACTORY.object();
	}
	
	public static void recycle(ACLMessage message) {
		FACTORY.recycle(message);
	}

	public void setSender(Individual s) {
		source = checkNotNull(s);
	}

	public void setPerformative(ACLPerformative perf) {
		performative = perf;
	}

	public void setStringContent(String stringContent) {
        this.content = new StringBuffer(stringContent);
        this.contentType = ContentType.STRING;
	}

	public void setByteSequenceContent(byte[] byteSequenceContent) {
        this.contentType = ContentType.BYTE_ARRAY;
        this.content = byteSequenceContent;
	}

	/**
	 * This method sets the StringContent of this ACLMessage to a Java object.
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
	 * @param s the object that will be used to set the StringContent of the ACLMessage.
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
	 * This method returns the StringContent of this ACLMessage when they have
	 * been written via the method <code>setContentObject</code>.
	 * It is not FIPA compliant so its usage is not encouraged.
	 * For example to read Java objects from the StringContent
	 * <PRE>
	 * ACLMessage msg = blockingReceive();
	 * try{
	 *  Date d = (Date)msg.getContentObject();
	 * }catch(UnreadableException e){}
	 * </PRE>
	 * 
	 * @return the object read from the StringContent of this ACLMessage
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
        this.content = o;
        this.contentType = ContentType.OTHER;
	}

	public Object getReferenceContent() {
		return content;
	}

	public void setReplyWith(String reply) {
		reply_with = reply; 
	}

	public void setInReplyTo(String reply) {
		in_reply_to = reply;
	}

	public void setEncoding(String str) {
		encoding = str;
	}

	public void setLanguage(String str) {
		language = str;
	}

	public void setOntology(String str) {
		ontology = str;
	}

	public void setProtocol( String str ) {
		protocol = str;
	}

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

	public Iterator<Individual> getAllReplyTo() {
		if (reply_to == null) {
			return Iterators.emptyIterator();
		}
		else {
			return reply_to.iterator();
		}
	}

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
	 * Reads <code>:StringContent</code> slot. <p>
	 * <p>Notice that, in general, setting a String StringContent and getting
	 * back a byte sequence StringContent - or viceversa - does not return
	 * the same value, i.e. the following relation does not hold
	 * <code>
	 * getByteSequenceContent(setByteSequenceContent(getStringContent().getBytes()))
	 * is equal to getByteSequenceContent()
	 * </code>
	 * @return The value of <code>:StringContent</code> slot. Guarantied to be not <code>null</code>
     */
	public String getStringContent() {
        switch (contentType) {
            case STRING:
                return new String((StringBuffer)content);
            case BYTE_ARRAY:
                return new String((byte[])content);
            default:
            case OTHER:
                return content.toString();
        }
	}

	public byte[] getByteSequenceContent() {
		switch (contentType) {
            default:
            case STRING:
                return content.toString().getBytes();
            case BYTE_ARRAY:
                return (byte[]) content;
        }
	}

	public String getReplyWith() {
		return reply_with;
	}

	public String getInReplyTo() {
		return in_reply_to;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getLanguage() {
		return language;
	}

	public String getOntology() {
		return ontology;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getConversationId() {
		return conversation_id;
	}

	public void addReceiver(Individual r) {
		dests.add(checkNotNull(r));
	}
	
	public void addReceivers(Collection<Individual> receivers) {
		dests.addAll(checkNotNull(receivers));
	}
	
	public void send(final ACLMessageTransmitter transmitter) {
        if (Strings.isNullOrEmpty(reply_with))
            generateReplyWith();

		transmitter.deliverMessage(this);
	}

	/**
	 * create a new ACLMessage that is a reply to this message.
	 * In particular, it sets the following parameters of the new message:
	 * receiver, language, ontology, protocol, conversation-id,
	 * in-reply-to, reply-with.
	 * The programmer needs to set the communicative-act and the StringContent.
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
			setReplyWith("X" + java.lang.System.currentTimeMillis());
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
        switch (contentType) {
            case BYTE_ARRAY:
               str.append(":StringContent" + " <BINARY> \n");
                break;
            case STRING:
                String content = getStringContent().trim();
				str.append(":StringContent" + " \"" + content + "\" \n");
                break;
            case NULL:
                 str.append(":StringContent" + " <Not set> \n");
                break;
            case OTHER:
                str.append(":StringContent" + " <OBJECT> \n");
                break;
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
		this.performative = ACLPerformative.NOT_UNDERSTOOD;
		this.content = NULL_CONTENT;
        this.contentType = ContentType.NULL;
		this.conversation_id = ++progressiveId;
		this.dests.clear();
		this.encoding = null;
		this.in_reply_to = null;
		this.language = null;
		this.ontology = null;
		this.protocol = null;
		this.reply_to = null;
		this.reply_with = null;
		this.source = null;
	}
	
	public ACLMessage createCopy() {
		ACLMessage ret = newInstance();
		
		ret.performative = this.performative;
        ret.content = this.content;
        ret.contentType = this.contentType;
		ret.conversation_id = this.conversation_id;
		ret.dests.clear();
		ret.dests.addAll(this.dests);
		ret.encoding = this.encoding;
		ret.in_reply_to = this.in_reply_to;
		ret.language = this.language;
		ret.ontology = this.ontology;
		ret.protocol = this.protocol;
		ret.reply_to = this.reply_to;
		ret.reply_with = this.reply_with;
		ret.source = this.source;
		
		return ret;
	}
}
