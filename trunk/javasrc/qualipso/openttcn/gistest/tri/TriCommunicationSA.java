package qualipso.openttcn.gistest.tri;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import qualipso.openttcn.gistest.CityBean;
import qualipso.openttcn.gistest.QueryingUtilities;


import com.meterware.httpunit.Button;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.openttcn.sdk.tri.Factory;
import com.openttcn.sdk.tri.StartHereSA;
/**
 * Responsible for communication with the SUT. 
 * Uses HttpUnit to handle the GISClient web interface and simple HTTP connections to send 
 * XML messages to GEOServer's REST web services interface. 
 */
public class TriCommunicationSA extends com.openttcn.sdk.tri.TriCommunicationSA {
	private qualipso.openttcn.gistest.QueryingUtilities qUtil = new QueryingUtilities();
	private static final long serialVersionUID = 6917827107004937619L;	
	   
//	private final String GEOSERVER_URL = "http://localhost:10002/geoserver/wfs";
//	private final String GISCLIENT_URL = "http://localhost:10001/GISClient3/";
	private final String GEOSERVER_URL = "http://150.254.173.202:8090/geoserver/wfs";
	private final String GISCLIENT_URL = "http://syros.eurodyn.com:18088/GISClient3/";
	       
	private final byte DELIMETER = 0x0D;
	private final String ERROR = "ERROR";
	private final String SUCCESS = "SUCCESS";
	
	/** 
	 * Does nothing and returns <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 * @return <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 */
	public org.etsi.ttcn.tri.TriStatus triSAReset() {
        return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);
    }
	/**
	 * Does nothing and returns <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 * @param testcase
	 * @param tsiList
	 * @return <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 */
	public TriStatus triExecuteTestcase(final TriTestCaseId testcase,
			final TriPortIdList tsiList) {
		return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);
	}
	
	/**
	 * Does nothing and returns <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 * @param componentPortId
	 * @param tsiPortId
	 * @return <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 */
	public TriStatus triMap(TriPortId componentPortId, TriPortId tsiPortId) {
		return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);
	}
	
	/**
	 * Does nothing and returns <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 * @param compPortId
	 * @param tsiPortId
	 * @return <code>org.etsi.ttcn.tri.TriStatus.TRI_OK</code>
	 */
	public org.etsi.ttcn.tri.TriStatus triUnmap(
            org.etsi.ttcn.tri.TriPortId compPortId,
            org.etsi.ttcn.tri.TriPortId tsiPortId) {
        return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);
    }
	
	/**
	 * Sends a message to the SUT and enqueues the answer so the Test Executable will pick it up.
	 * @param componentId the id of the component sending the message
	 * @param tsiPortId  the id of the port sending the message
 	 * @param address the address specified in the <i>to</i> clause of the <i>send()</i> operation of the TTCN-3 code  
	 * @param sendMessage message to send, encoded by the Codec. 
	 */
	public TriStatus triSend(TriComponentId componentId,
			final TriPortId tsiPortId, TriAddress address,
			final TriMessage sendMessage) {
        //Get the TSI port in use. The String argument is the port type declared in TTCN-3 code
        org.etsi.ttcn.tri.TriPortId p = Factory.createTriPortId("tsiPort");
        //get the active test server
        org.etsi.ttcn.tri.TriCommunicationTE te = StartHereSA.getRequestServer();
        //get the codec 
        org.etsi.ttcn.tci.TciCDRequired cd = com.openttcn.sdk.tci.StartHereCD.getRequestServer();
        org.etsi.ttcn.tri.TriComponentId c =
            Factory.createTriComponentId(componentId.getComponentId());
        
        //set the SUT address to be sent to the decoder
        org.etsi.ttcn.tci.RecordValue sutAddress = 
        	(org.etsi.ttcn.tci.RecordValue)cd.getTypeForName("address").newInstance();
        sutAddress.setField("host",
        		com.openttcn.sdk.tci.Utilities.charstringToTciValue("sutHost"));
        
        org.etsi.ttcn.tri.TriAddress encodedAddress = 
        	com.openttcn.sdk.tri.Factory.createTriAddress(
        			((com.openttcn.sdk.tci.Value)sutAddress).opaque);
		HTMLParserFactory.setReturnHTMLDocument(false);
		String errorMessage = ERROR;
		byte[] encMessage = sendMessage.getEncodedMessage();
		HashMap<String, String> hm = null;
		if (encMessage!=null) {
			try {
				hm = getParametersFromMessage(encMessage);
			} catch (Exception e) {
				errorMessage = errorMessage + e.getMessage();
				TriMessage m = Factory.createTriMessage(errorMessage.getBytes());
				te.triEnqueueMsg(p, encodedAddress, null, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_ERROR);		
			}			
		}   
		if (hm!=null) {			
			String requestType = (String) hm.get("requestType");
			if (requestType.equalsIgnoreCase("addOldCity")) {
				/* Testing GISClient update
				 * 1. Insert the original city directly at GEOServer
				 * 2. Find the city using GISClient and make the updates
				 * 3. Retrieve the city from GEOServer
				 * 4. Compare the city received with the one updated
				 */
				String insertCityQuery = "";
				CityBean cb = new CityBean();
				cb.setAction("addCity");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String insertQuery = qUtil.getInsertCityQuery(cb);
				String response = sendQueryToServer(insertQuery);
				String message = "";
				if (response.contains(ERROR)) {
					message="ERROR";
				} else {
					message="SUCCESS";
				}
				TriMessage m = Factory.createTriMessage(message.getBytes());
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);		
			} else if (requestType.equals("addCityGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGIS");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));				
				String response = insertCityUsingGISClient(cb);
				String message = "";
				if (response.contains(ERROR)) {
					message=ERROR;
				} else {
					message=SUCCESS;
				}
				TriMessage m = Factory.createTriMessage(message.getBytes());
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);	
			} else if (requestType.equals("addCityGEO")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));				
				String insertCityQuery = qUtil.getInsertCityQuery(cb);
				String serverResponse = sendQueryToServer(insertCityQuery);
				String result =  "";
				if (serverResponse.contains("SUCCESS")) {
					result = SUCCESS;
				} else {
					result = ERROR;
				}
				TriMessage m = Factory.createTriMessage(result.getBytes());
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);					
			} else if (requestType.equalsIgnoreCase("updateCity")) {
				CityBean oldCity = new CityBean();
				CityBean newCity = new CityBean();
				
				oldCity.setName(hm.get("oldCityName"));
				oldCity.setLatitude(hm.get("oldLatitude"));
				oldCity.setLongitude(hm.get("oldLongitude"));
				oldCity.setAdminName(hm.get("oldAdminName"));
				oldCity.setCountryName(hm.get ("oldCountryName"));
				oldCity.setStatus(hm.get("oldStatus"));
				oldCity.setPopClass(hm.get("oldPopClass"));
				
				newCity.setName(hm.get("newCityName"));
				newCity.setLatitude(hm.get("newLatitude"));
				newCity.setLongitude(hm.get("newLongitude"));
				newCity.setAdminName(hm.get("newAdminName"));
				newCity.setCountryName(hm.get ("newCountryName"));
				newCity.setStatus(hm.get("newStatus"));
				newCity.setPopClass(hm.get("newPopClass"));
				
				String response = updateCityUsingGISClient(oldCity, newCity);
				TriMessage m = Factory.createTriMessage(response.getBytes());
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);		
			}  else if (requestType.equalsIgnoreCase("deleteGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String response = deleteCityUsingGISClient(cb);
				byte[] b = response.getBytes();
				TriMessage m = Factory.createTriMessage(b);
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);	
				
			} else if (requestType.equalsIgnoreCase("deleteGEO")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String query = qUtil.getDeleteCityQuery(cb);
				String serverResponse = sendQueryToServer(query);
				String result =  "";
				if (serverResponse.contains("SUCCESS")) {
					result = SUCCESS;
				} else {
					result = ERROR;
				}
				TriMessage m = Factory.createTriMessage(result.getBytes());
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);					
			} 
			else if (requestType.equalsIgnoreCase("retrieveCity")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(null);
				cb.setLongitude(null);
				cb.setAdminName(hm.get("adminName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String query = qUtil.getRetrieveCityQuery(cb);
				String serverResponse = sendQueryToServer(query);	
				CityBean receivedCity = createCityBean(serverResponse, "retrieveCity");
				byte[] b;
				try {
					ByteArrayOutputStream array_out = new ByteArrayOutputStream();
					ObjectOutputStream obj_out = new ObjectOutputStream(array_out);
					obj_out.writeObject(receivedCity);
					b = array_out.toByteArray();					
				} catch (IOException e) {
					String m = "error" + e.getStackTrace();
					b = m.getBytes();					
				}
				TriMessage m = Factory.createTriMessage(b);
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);							
			} else if (requestType.equalsIgnoreCase("retrieveCityGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("retrieveCityGIS");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				CityBean responseCity = retrieveCityFromGISClient(cb);
				byte[] b;
				if (responseCity != null) {
					try {
						ByteArrayOutputStream array_out = new ByteArrayOutputStream();
						ObjectOutputStream obj_out = new ObjectOutputStream(array_out);
						obj_out.writeObject(responseCity);
						b = array_out.toByteArray();					
					} catch (IOException e) {
						String m = ERROR + e.getStackTrace();
						b = m.getBytes();					
					}					
				} else {
					b="ERROR. City not found".getBytes();
				}
				TriMessage m = Factory.createTriMessage(b);
				te.triEnqueueMsg(p, encodedAddress, c, m);
				return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);			
			}
			errorMessage += "Unknown action requested.";
			TriMessage m = Factory.createTriMessage(errorMessage.getBytes());
			te.triEnqueueMsg(p, encodedAddress, c, m);
			return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);	
		} else {
			errorMessage += "Unable to get the decoder of the request parameters. " + new String(encMessage);
			TriMessage m = Factory.createTriMessage(errorMessage.getBytes());
			te.triEnqueueMsg(p, encodedAddress, c, m);
			return Factory.createTriStatus(org.etsi.ttcn.tri.TriStatus.TRI_OK);	
		}
	}
	// reads a TCCN-3 record (encoded as a delimited string) and stores it to a HashMap
	/**
	 * @param message a byte array containing the message as it came from the encode function of the Codec
	 * @return a <code>HashMap</code> with key being the parameter name and the value being the value of the parameter 
	 */
	private HashMap<String, String> getParametersFromMessage(byte[] message) throws Exception {
		HashMap<String, String> values = new HashMap<String, String>();
		int lastDelimiterIndex = 0;
		boolean isName = true; // is what we are currently reading the *name* of
							   // a field or its *value*?
		String paramName = "";
		for (int i = 0; i < message.length; i++) {
			if (message[i] == DELIMETER) {
				byte[] nArr = new byte[i - lastDelimiterIndex];
				System.arraycopy(message, lastDelimiterIndex, nArr, 0, nArr.length);
				lastDelimiterIndex = i + 1;
				if (isName) {
					paramName = new String(nArr);
					isName = false;
				} else {
					String val = new String(nArr);
					values.put(paramName, val);
					isName = true;
				}
			}
		}
		return values;
	}
	
	/**
	 * Sends an XML message to GEOServer's REST web services interface and returns the GEOServer's response
	 * @param xmlQuery a WFS-T compatible XML message. It will be sent to GEOServer
	 * @return GEOServer's response after receiving the message
	 */
	private String sendQueryToServer(String xmlQuery) {
		StringBuffer response = new StringBuffer() ;
		try {	
			URL geoserverURL = new URL(GEOSERVER_URL);
			HttpURLConnection conn = (HttpURLConnection)geoserverURL.openConnection();	
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "User-agent", "tester" );
			conn.setRequestProperty("Content-Type", "application/xml"); 
			conn.setInstanceFollowRedirects(true);
			
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream()) ;
			out.write(xmlQuery);
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";	
			while ((line=in.readLine())!=null) {	
				response.append(line);
			}
			in.close();
		} catch (Exception e) {
			//  something
			response.append("error: " + e.toString());
		}		
		return response.toString();
	}

	/**
	 * Inserts a City feature through GISCliens web interface (using HttpUnit to manipulate it)  
	 * @param cb a CityBean containing the parameter values of the City feature 
	 * @param urlString this is either SUCCESS or ERROR
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */
	private String insertCityUsingGISClient(CityBean cb) {
		String response = ERROR;
		String cityName = cb.getName();
		String latitude = cb.getLatitude();
		String longitude = cb.getLongitude();
		String adminName = cb.getAdminName();
		String countryName = cb.getCountryName();
		String status = cb.getStatus();
		String popClass = cb.getPopClass();
		try {
			// navigate to add new city form
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebLink addCityLink = citiesFrame.getLinkWith("Add new city");
			addCityLink.click();
			
			mainPage = wc.getCurrentPage();
			WebResponse addCityFrame = wc.getFrameContents("inputs");
			
			WebForm cityForm = addCityFrame.getForms()[0];			
			cityForm.setParameter("CITY_NAME",cityName);
			cityForm.setParameter("LATITUDE",latitude);
			cityForm.setParameter("LONGITUDE",longitude);
			cityForm.setParameter("ADMIN_NAME",adminName);
			cityForm.setParameter("CNTRY_NAME",countryName);
			cityForm.setParameter("STATUS",status);
			cityForm.setParameter("POP_CLASS",popClass);
			SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Insert");
			citySubmitButton.click();				
			mainPage = wc.getCurrentPage();
			//Now we have to navigate to actions and commit the new city
			WebResponse actionsFrame = wc.getFrameContents("methods");
			WebLink actionsLink = actionsFrame.getLinkWith("Actions");
			actionsLink.click();
			mainPage = wc.getCurrentPage();
			WebResponse commitFrame = wc.getFrameContents("inputs");
			WebForm commitForm = commitFrame.getForms()[0];
			SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
			commitCityButton.click(); //click on the first commit button
			mainPage = wc.getCurrentPage();
			WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
			WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
			Button verifyCommitButton = verifyCommitForm.getButtons()[0];
			verifyCommitButton.click(); //click on the second commit button
			mainPage = wc.getCurrentPage();
			WebResponse responseFrame = wc.getFrameContents("inputs");
			String xmlResponse = responseFrame.getText();
			if (xmlResponse.toLowerCase().contains("success")) {
				response = SUCCESS;
			} else {
				response = ERROR;
			}		

		} catch(Exception e) {
			response = ERROR;
		}				
		return response;
	}
	/** 
	 * Creates a CityBean object after parsing the XML message returned by GEOServer after a retrieve request.
	 * The CityBean will be send to the Codec to create a TTCN-3 CityType object
	 * @param xmlText GEOServer's XML message response 
	 * @param action the action parameter that must be set to the CityBean.
	 * @return the CityBean that will be sent to the Codec.
	 */
	private CityBean createCityBean(String xmlText, String action) {
		CityBean cb = new CityBean();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new ByteArrayInputStream(xmlText.getBytes()));
			List cities = doc.selectNodes("/wfs:FeatureCollection/gml:featureMember/topp:tasmania_cities");
			if (cities!=null) {
				if (cities.size() > 1 || cities.size() < 1) {
					//throw new Exception("Error: found " + cities.size() + " cities. Must be exactly one." );							
				}
				Node city = (Node)cities.get(0);
				cb.setAction(action);
				cb.setName(city.valueOf("topp:CITY_NAME"));
				String coords = city.valueOf("topp:the_geom/gml:MultiPoint/gml:pointMember/gml:Point/gml:coordinates");
				String[] coordArray = coords.split(",");
				String longitudeString = coordArray[0];
				String latitudeString = coordArray[1];
				cb.setLatitude(latitudeString);
				cb.setLongitude(longitudeString);
				cb.setAdminName(city.valueOf("topp:ADMIN_NAME"));
				cb.setCountryName(city.valueOf("topp:CNTRY_NAME"));
				cb.setStatus(city.valueOf("topp:STATUS"));
				cb.setPopClass(city.valueOf("topp:POP_CLASS"));
			}			
		} catch (Exception e) {
			
		}
		return cb;
	}
	/** 
	 * Update a City through GISClient's web interface (using HttpUnit)
	 * @param oldCity the City feature that already exists and will be updated
	 * @param newCity a CityBean that contains the new values to set to the City feature
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */
	private String updateCityUsingGISClient(CityBean oldCity, CityBean newCity) {
		String response = ERROR;
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			// find the row that has the old city
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebTable citiesTable = citiesFrame.getTables()[0];
			
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			String lastline = "" + allTable.length;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				lastline +=":" + aLine[0] + aLine[1]+aLine[2]+aLine[3];
				if (aLine.length>3 && 
					aLine[0].trim().equals(oldCity.getName()) &&
					aLine[1].trim().equals(oldCity.getCountryName()) &&
					aLine[2].trim().equals(oldCity.getAdminName()) &&
					aLine[3].trim().equals(oldCity.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				editLink.click();
				// find the row that has the old city
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");
				WebForm cityForm = editFrame.getForms()[0];			
				cityForm.setParameter("CITY_NAME",newCity.getName());
				cityForm.setParameter("LATITUDE",newCity.getLatitude());
				cityForm.setParameter("LONGITUDE",newCity.getLongitude());
				cityForm.setParameter("ADMIN_NAME",newCity.getAdminName());
				cityForm.setParameter("CNTRY_NAME",newCity.getCountryName());
				cityForm.setParameter("STATUS",newCity.getStatus());
				cityForm.setParameter("POP_CLASS",newCity.getPopClass());
				SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Update");
				citySubmitButton.click();
				mainPage = wc.getCurrentPage();
				//Now we have to navigate to actions and commit the new city
				WebResponse actionsFrame = wc.getFrameContents("methods");
				WebLink actionsLink = actionsFrame.getLinkWith("Actions");
				actionsLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse commitFrame = wc.getFrameContents("inputs");
				WebForm commitForm = commitFrame.getForms()[0];
				SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
				commitCityButton.click(); //click on the first commit button
				mainPage = wc.getCurrentPage();
				WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
				WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
				Button verifyCommitButton = verifyCommitForm.getButtons()[0];
				verifyCommitButton.click(); //click on the second commit button
				mainPage = wc.getCurrentPage();
				WebResponse responseFrame = wc.getFrameContents("inputs");
				String xmlResponse = responseFrame.getText();
				if (xmlResponse.toLowerCase().contains("success")) {
					response = SUCCESS;
				} else {
					response = ERROR;
				}						
			} else {
				response = ERROR + ": no such element";
			}
		} catch (Exception e) {
			response = ERROR + e.toString();
		}		
		return response;		
	}
	/** 
	 * Deletes a City feature through GISClient's web interface (using HttpUnit).
	 * @param cb A CityBean containing the parameters of the City feature to be deleted
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */
	private String deleteCityUsingGISClient(CityBean cb) {
		String response = "";
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			response += citiesFrame.getText() ;
			WebTable citiesTable = citiesFrame.getTables()[0];
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				if (aLine.length>3 && 
					aLine[0].trim().equals(cb.getName()) &&
					aLine[1].trim().equals(cb.getCountryName()) &&
					aLine[2].trim().equals(cb.getAdminName()) &&
					aLine[3].trim().equals(cb.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				editLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");			
				WebForm cityForm = editFrame.getForms()[1];
				SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Delete");
				citySubmitButton.click();
				mainPage = wc.getCurrentPage();
				//Now we have to navigate to actions and commit the new city
				WebResponse actionsFrame = wc.getFrameContents("methods");
				WebLink actionsLink = actionsFrame.getLinkWith("Actions");
				actionsLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse commitFrame = wc.getFrameContents("inputs");
				WebForm commitForm = commitFrame.getForms()[0];
				SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
				commitCityButton.click(); //click on the first commit button
				mainPage = wc.getCurrentPage();
				WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
				WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
				Button verifyCommitButton = verifyCommitForm.getButtons()[0];
				verifyCommitButton.click(); //click on the second commit button
				mainPage = wc.getCurrentPage();
				WebResponse responseFrame = wc.getFrameContents("inputs");
				String xmlResponse = responseFrame.getText();
				if (xmlResponse.toLowerCase().contains("success")) {
					response = SUCCESS;
				} else {
					response = ERROR;
				}					
			} else {
				response = ERROR;
			}		
		} catch (Exception e) {
			response = ERROR;
		}		
		return response;
	}	
	/** 
	 * Retrieves a City feature from GISClient's web interface.
	 * @param cb A CityBean object containing the parameters of the City feature to be retrieved.
	 * @return a CityBean object containing the parameters of the retrieved City feature. If none is found it returns <code>null</code>
	 */
	private CityBean retrieveCityFromGISClient(CityBean cb) {
		String response =ERROR;
		CityBean cityFound = new CityBean();
		
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			// find the row that has the old city
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebTable citiesTable = citiesFrame.getTables()[0];
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			String lastline = "" + allTable.length;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				if (aLine.length>3 && 
					aLine[0].trim().equals(cb.getName()) &&
					aLine[1].trim().equals(cb.getCountryName()) &&
					aLine[2].trim().equals(cb.getAdminName()) &&
					aLine[3].trim().equals(cb.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				response += editLink.getText();
				editLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");
				WebForm cityForm = editFrame.getForms()[0];			
				cityFound.setAction("retrieveCityGIS");
				cityFound.setName(cityForm.getParameterValue("CITY_NAME"));
				cityFound.setLatitude(cityForm.getParameterValue("LATITUDE"));
				cityFound.setLongitude(cityForm.getParameterValue("LONGITUDE"));
				cityFound.setAdminName(cityForm.getParameterValue("ADMIN_NAME"));
				cityFound.setCountryName(cityForm.getParameterValue("CNTRY_NAME"));
				cityFound.setStatus(cityForm.getParameterValue("STATUS"));
				cityFound.setPopClass(cityForm.getParameterValue("POP_CLASS"));
				return cityFound;
			} else {
				return null;
			}			
		} catch (Exception e) {
			return null;
		}		
	}

}

