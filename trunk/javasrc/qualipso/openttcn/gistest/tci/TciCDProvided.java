package qualipso.openttcn.gistest.tci;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

import org.etsi.ttcn.tci.BooleanValue;
import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Value;
import qualipso.openttcn.gistest.CityBean;
import com.openttcn.sdk.tci.CharstringValue;

/**
 * Encodes the messages sent from the TTCN-3 executable into a byte array and
 * decodes the messages received from the SUT into a Value object that can be
 * handled by the TTCN-3 executable. 
 */
public class TciCDProvided implements org.etsi.ttcn.tci.TciCDProvided {
	final byte DELIMETER = 0x0D;
	/**
	 * Receives a <code>org.etsi.ttcn.tci.Value</code> object and  
	 * transforms it to a byte array. The byte array is then encapsulated
	 * in a <code>org.etsi.ttcn.tri.TriMessage</code>
	 * @param value the message received from the TTCN-3 executable.
	 * @return the message encoded as a byte array
	 * @see http://wiki.openttcn.com/media/index.php/OpenTTCN/Developer_corner/Creating_adapter_with_Java_SDK#Creating_encoder
	 */
    public org.etsi.ttcn.tri.TriMessage encode(org.etsi.ttcn.tci.Value value) {    	    	
    	// We first must tell if we have to encode an address or a message.    	    
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    String errMsg = "";
		if (value != null) {
			org.etsi.ttcn.tci.Type valueType = value.getType();
			String typeName = valueType.getName();
			int typeClass = valueType.getTypeClass();
			if (typeClass == org.etsi.ttcn.tci.TciTypeClass.ADDRESS) {
				return com.openttcn.sdk.tri.Factory.createTriMessage(((com.openttcn.sdk.tci.Value)value).opaque);
			}
			if (typeName.equals("CityType") || typeName.equals("UpdateCity") ) {
				String[] fieldNames = ((RecordValue)value).getFieldNames();
				if (fieldNames!=null && fieldNames.length>0) {
					for (int i=0; i<fieldNames.length; i++) {
						errMsg = errMsg + "1";
						Value fieldValue = ((RecordValue)value).getField(fieldNames[i]);
						
						if (fieldValue.notPresent()) {
							// cannot have *any* or *omit* in send message
							errMsg= errMsg +"||"+fieldNames[i]+"-->" +fieldValue+ 
							"--" + "!notPresent():" +!fieldValue.notPresent() +
							"\n"+"cannot have *any* or *omit* in sent record's fields";
						} else {
							byte[] name = fieldNames[i].getBytes();
							baos.write(name,0,name.length);
							baos.write(DELIMETER);
							String stringValue= "";
							switch (fieldValue.getType().getTypeClass()) {
								case TciTypeClass.INTEGER:
									stringValue = Integer.toString(((IntegerValue)fieldValue).getInteger());
								    break;						
								case TciTypeClass.BOOLEAN:
									stringValue = Boolean.toString(((BooleanValue)fieldValue).getBoolean());
									break;
								case TciTypeClass.FLOAT:
									stringValue = Float.toString(((FloatValue)fieldValue).getFloat());
								    break;
								case TciTypeClass.CHARSTRING:								
									stringValue = ((com.openttcn.sdk.tci.CharstringValue)fieldValue).getValue();
									break;
								case TciTypeClass.UNIVERSAL_CHARSTRING:
									stringValue = ((com.openttcn.sdk.tci.UniversalCharstringValue)fieldValue).getValue();
									break;											
								case TciTypeClass.HEXSTRING:
								    stringValue = ((com.openttcn.sdk.tci.HexstringValue)fieldValue).getValue();
								    break;						
								default:
									stringValue = "wrongTypeOrValue";
							}	
							byte[] valueToByteArray = stringValue.getBytes();
							baos.write(valueToByteArray,0,valueToByteArray.length);
							baos.write(DELIMETER);
							try {
								baos.flush();
							} catch (Exception e) {
								errMsg = "IOErrorWhileFlushing";
									
								return com.openttcn.sdk.tri.Factory.createTriMessage(errMsg.getBytes());
							}
						}
					}
					if (baos.size()>0) {
						return com.openttcn.sdk.tri.Factory.createTriMessage(baos.toByteArray());	
					} else {
						errMsg = errMsg + "nothing written to decoder output";
						return com.openttcn.sdk.tri.Factory.createTriMessage(errMsg.getBytes());
						
					}
				} else {
					errMsg = "record has no fields";
					return com.openttcn.sdk.tri.Factory.createTriMessage(errMsg.getBytes());
				}
			}
	    }
		errMsg = "wrongTemplate";		
	    return com.openttcn.sdk.tri.Factory.createTriMessage(errMsg.getBytes());
    }
	/**
	 * Reads the messages returned from the SUT and according to their type
	 * transforms it into an appropriate <code>org.etsi.ttcn.tci.Value</code>
	 * It is called when a receive operation is called.
	 * @param rcvdMessage  message received from the SUT and enqueued by the Adapter
	 * @param decodingHypothesis the type of message that TTCN-3 code expects to receive
	 * @return the message decoded. It can be a <code>Charstring</code> or <code>CityType</code> 
	 * @see http://wiki.openttcn.com/media/index.php/OpenTTCN/Developer_corner/Creating_adapter_with_Java_SDK#Creating_decoder
	 */
    public org.etsi.ttcn.tci.Value decode(
        org.etsi.ttcn.tri.TriMessage rcvdMessage,
        org.etsi.ttcn.tci.Type decodingHypothesis) {    	
    	org.etsi.ttcn.tci.TciCDRequired cd = com.openttcn.sdk.tci.StartHereCD.getRequestServer();
    	if (decodingHypothesis == null) {
    		try {
	       		// Since a valid decoding hypothesis is not given, we have to guess what kind of response
    			// we get. 
	    		byte[] messageArray = rcvdMessage.getEncodedMessage();
				// Assume decoding for map details
	    		Object obj = null;
	    		try {
	    			ByteArrayInputStream array_in = new ByteArrayInputStream(rcvdMessage.getEncodedMessage());
	    			ObjectInputStream obj_in = new ObjectInputStream(array_in);			
					obj = obj_in.readObject();
				} catch (Exception e) {
					obj=null;
				}
				if (obj instanceof CityBean) {
					final RecordValue cityTypeValue = (org.etsi.ttcn.tci.RecordValue)cd.getTypeForName("CityType").newInstance();				
					final Value actionValue = cityTypeValue.getField("requestType");				
					final Value cityNameValue = cityTypeValue.getField("cityName");
					final Value longitudeValue = cityTypeValue.getField("longitude");
					final Value latitudeValue = cityTypeValue.getField("latitude");
					final Value adminValue = cityTypeValue.getField("adminName");
					final Value countryValue = cityTypeValue.getField("countryName");
					final Value statusValue = cityTypeValue.getField("status");
					final Value popValue = cityTypeValue.getField("popClass");
					
					CharstringValue actionChar = (CharstringValue)actionValue.getType().newInstance();
					CharstringValue cityNameChar = (CharstringValue)cityNameValue.getType().newInstance();
					CharstringValue longitudeFloat = (CharstringValue)longitudeValue.getType().newInstance();
					CharstringValue latitudeFloat = (CharstringValue)latitudeValue.getType().newInstance();
					CharstringValue adminChar = (CharstringValue)adminValue.getType().newInstance();
					CharstringValue countryChar = (CharstringValue)countryValue.getType().newInstance();
					CharstringValue statusChar = (CharstringValue)statusValue.getType().newInstance();
					CharstringValue popChar = (CharstringValue)popValue.getType().newInstance();
					CityBean cb = (CityBean)obj;
					if (cb!=null) {
						String action = cb.getAction();
						String cityName = cb.getName();
						String longitude = cb.getLongitude();
						String latitude = cb.getLatitude();
						String adminName = cb.getAdminName();
						String country = cb.getCountryName();
						String status = cb.getStatus();
						String pop = cb.getPopClass();
						
						if (action==null || action.equals("")) {action = "";}
						if (cityName==null || cityName.equals("")) {cityName = "";}
						if (longitude==null || longitude.equals("")) {longitude = "0.0";}
						if (latitude==null || latitude.equals("")) {latitude = "0.0";}
						if (adminName==null || adminName.equals("")) {adminName = "";}
						if (country==null || country.equals("")) {country = "";}
						if (status==null || status.equals("")) {status = "";}
						if (pop==null || pop.equals("")) {pop = "";}
						
						actionChar.setValue(action);
						cityNameChar.setValue(cityName);
						longitudeFloat.setValue(longitude);
						latitudeFloat.setValue(latitude);
						adminChar.setValue(adminName);
						countryChar.setValue(country);
						statusChar.setValue(status);
						popChar.setValue(pop);
					} else {
						return null;
					}				
					cityTypeValue.setField("requestType", actionChar);
					cityTypeValue.setField("cityName", cityNameChar);
					cityTypeValue.setField("latitude", latitudeFloat);
					cityTypeValue.setField("longitude", longitudeFloat);
					cityTypeValue.setField("adminName", adminChar);
					cityTypeValue.setField("countryName", countryChar);
					cityTypeValue.setField("status", statusChar);
					cityTypeValue.setField("popClass", popChar);
					return cityTypeValue;
								
				} else {					
					String messageAsString = new String(rcvdMessage.getEncodedMessage());
					if (messageAsString !=null) {
						// If it contains the words SUCCESS or ERROR it must be turned to a CharstringValue
						CharstringValue charVal = (CharstringValue)cd.getCharstring().newInstance();
						charVal.setValue(messageAsString);						
						return charVal;					
					} 
				}
    		} catch (Exception e) {
    			e.printStackTrace(System.out);
    		}
    	} else {
    		String hypName = decodingHypothesis.getName();
    		if (hypName.equals("address")) {
    			try{
	    			com.openttcn.sdk.tci.RecordValue addrValue = new com.openttcn.sdk.tci.RecordValue();
	    	        addrValue.opaque = rcvdMessage.getEncodedMessage();
	    	        return addrValue;
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
		return null;
    }    
}

