package gateway.middlewarewebservice.resource;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import pk.vaulsys.apigateway.customer.AuthMPGSTransaction;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.TransactionCodes;
import pk.vaulsys.apigateway.protocols.webservice.base.APIVersion;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.webservice.AppWSServer;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;
import pk.vaulsys.apigateway.wfe.process.MainAppWSProcess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Path("/SadadMiddlewareServiceGateway/webresources/app")
public class AppWebResource {

    private static final Logger logger = LogManager.getLogger(AppWebResource.class);

    //m.rehman: trying to get ip address of a client
    @Context
    private HttpServletRequest request;

//	@GET
//	@Path("/ping")
//	public MWWsEntity getServerTime() {
//		System.out.println("Vaulsys RESTful Service is running ==> ping");
//		logger.info("Vaulsys RESTful Service is running ==> ping");
//		MWWsEntity obj = new MWWsEntity();
//		obj.setMobilenumber("MobileNumber");
//		AppContact appContact = new AppContact();
//		appContact.setMobilenumber("mobile1");
//		AppContact appContact2 = new AppContact();
//		appContact2.setMobilenumber("mobile2");
//		AppContact appContact3 = new AppContact();
//		appContact3.setMobilenumber("mobile3");
//		List<AppContact> lstappcontact = new ArrayList<>();
//		lstappcontact.add(appContact);
//		lstappcontact.add(appContact2);
//		lstappcontact.add(appContact3);
//		obj.setContactlist(lstappcontact);
//		return obj;
//	}

    @GET
    @Path("/ping")
    public String getServerTime() {
        //System.out.println("Vaulsys RESTful Service for RAW Bank App is running ==> ping");
        logger.info("Vaulsys RESTful Service for Sadad App is running ==> ping" + new Date().toString());
        return "received ping on " + new Date().toString();
    }

    @Path("/signup")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity SignUp(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsModel,
            @Context UriInfo uriInfo
    ) throws Exception {
        String apiPath = uriInfo.getRequestUri().getPath();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (wsModel == null) {
            logger.error("API {} | No data received from request", apiPath);
            wsModel = new AppWsEntity();
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }

        try {
            logger.info("API {} | Request received", apiPath);
            wsModel.setServicename(TransactionCodes.CREATE_WALLET);
            wsModel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsModel, loginID, loginPassword, auth);
            return executor.submit(process).get();
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", apiPath);
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/generatetruidtoken")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity getTrueIDToken(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsModel,
            @Context UriInfo uriInfo
    ) throws Exception {
        String apiPath = uriInfo.getRequestUri().getPath();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (wsModel == null) {
            logger.error("API {} | No data received from request", apiPath);
            wsModel = new AppWsEntity();
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }

        try {
            logger.info("API {} | TrueID request received", apiPath);

            wsModel.setServicename(TransactionCodes.GENERATE_TRUID_TOKEN);
            wsModel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(
                    AppWSServer.id++,
                    wsModel,
                    loginID,
                    loginPassword,
                    auth
            );
            return executor.submit(process).get();
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", apiPath);
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            executor.shutdownNow();
        }
    }

    @Path("/confirmdeviceunbinding")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity confirmDeviceUnbinding(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsModel,
            @Context UriInfo uriInfo
    ) throws Exception {
        String apiPath = uriInfo.getRequestUri().getPath();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (wsModel == null) {
            logger.error("API {} | No data received from request", apiPath);
            wsModel = new AppWsEntity();
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }

        try {
            logger.info("API {} | Request received", apiPath);
            wsModel.setServicename(TransactionCodes.CONFIRM_DEVICE_UNBINDING);
            wsModel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsModel, loginID, loginPassword, auth);
            return executor.submit(process).get();
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", apiPath);
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/fetchunbindingreasons")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity fetchUnbindingReasons(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsModel,
            @Context UriInfo uriInfo
    ) throws Exception {
        String apiPath = uriInfo.getRequestUri().getPath();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (wsModel == null) {
            logger.error("API {} | No data received from request", apiPath);
            wsModel = new AppWsEntity();
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }

        try {
            logger.info("API {} | Request received", apiPath);
            wsModel.setServicename(TransactionCodes.FETCH_UNBINDING_REASONS);
            wsModel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsModel, loginID, loginPassword, auth);
            return executor.submit(process).get();

        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", apiPath);
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            return wsModel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/login")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LogIn(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LogIn Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("LogIn");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //TODO Raza REMOVE after test
			/*
			if(wsmodel.getMobilenumber().equals("00243893767829"))
			{
				wsmodel.getSecurityparams().setFirebasetoken("c8rbzF3B0lo:APA91bHpnVBZa14bie7vHkexHswo2JT46CJcodrjxsfOLt7rjiavybbaViYyoCktE8Lv91htFMnFNhUuz7S1F0vN01vC9ujp09pNJY80Wp9HFjcU0hX4c2hKIWPwBGKeb43XGwN2tPL6");
			}
			else if(wsmodel.getMobilenumber().equals("00243851676070"))
			{
				wsmodel.getSecurityparams().setFirebasetoken("");
			}
			else if(wsmodel.getMobilenumber().equals("00243123456789"))
			{
				wsmodel.getSecurityparams().setFirebasetoken("e9RCSXVSzQ8:APA91bENcHj0xInCot65dWEi5mVfZYI-CwYxUgBTMPhfNGmWXQBejISDHTQR7wBquWI6Z6EZ-jvWTxE6RAjj61bKX5Or378Ynu9K7QUJO2SBq73vGOr3rovXQYN6nBOhUIMAY_iCZ2YJ");
			}
			else if(wsmodel.getMobilenumber().equals("00243900330222"))
			{
				wsmodel.getSecurityparams().setFirebasetoken("e9RCSXVS222:APA91bENcHj0xInCot65dWEi5mVfZYI-CwYxUgBTMPhfNGmWXQBejISDHTQR7wBquWI6Z6EZ-jvWTxE6RAjj61bKX5Or378Ynu9K7QUJO2SBq73vGOr3rovXQYN6nBOhUIMAY_iCZ2YJ");
			}
			else if(wsmodel.getMobilenumber().equals("00243852634592"))
			{
				wsmodel.getSecurityparams().setFirebasetoken("f3NKdm7MLYo:APA91bEAPDb80AXR_0R1TJ9YbQomAFAnvA24BTTVTdLKOm9HVkFu056WUS2bIlWS8oY-a33l2qt1Wh9OdqAiFhrqJwBUXjMMF-t4kpkKHE8q-O41KWj1KYdllOnoGR_hDfV95KbmoSPK");
			}
			else
			{
				wsmodel.getSecurityparams().setFirebasetoken("iCZ2YJ" + wsmodel.getMobilenumber());
			}*/
            //TODO Raza REMOVE after test

            ////ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);


            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            wsmodel.setPassword(null);

            if (!(wsmodel.getRespcode().equals(ISOResponseCodes.MW_APPROVED))
                    && !(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
                    && !(wsmodel.getRespcode().equals(ISOResponseCodes.MW_OTP_REQUIRED))
                    && !(wsmodel.getRespcode().equals(ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED))) {
                wsmodel.setMobilenumber(null);
                wsmodel.setUsername(null);
            }
            return wsmodel;
        } catch (Exception e) {
            logger.error("Exception caught while executing LogIn call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LogIn reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/logout")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LogOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LogOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("LogOut");
            //wsmodel.setApiname("LogOut");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing LogOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LogOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changepassword")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangePassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangePassword Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangePassword");
            //wsmodel.setApiname("ChangePassword");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ChangePassword call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangePassword reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changeusername")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeUserName(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeUserName Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeUserName");
            //wsmodel.setApiname("ChangeUserName");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeUserName call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeUserName reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changeusernamepassword")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeUserNamePassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeUserNamePassword Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeUserNamePassword");
            //wsmodel.setApiname("ChangeUserNamePassword");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeUserNamePassword call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeUserNamePassword reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/resetpassword")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ResetPassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ResetPassword Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ResetPassword");
            //wsmodel.setApiname("ResetPassword");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ResetPassword call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ResetPassword reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/resetusername")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ResetUserName(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ResetUserName Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ResetUserName");
            //wsmodel.setApiname("ResetUserName");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ResetUserName call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ResetUserName reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changeresetpassword")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeResetPassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeResetPassword Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeResetPassword");
            //wsmodel.setApiname("ChangeResetPassword");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeResetPassword call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeResetPassword reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changeresetusername")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeResetUserName(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeResetUserName Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeResetUserName");
            //wsmodel.setApiname("ChangeResetUserName");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeResetUserName call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeResetUserName reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/retrieveotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RetrieveOtp(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource RetrieveOtp Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("RetrieveOtp");
            //wsmodel.setApiname("ChangeResetUserName");
            wsmodel.setIncomingip(request.getRemoteAddr());

//			ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing RetrieveOtp call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending RetrieveOtp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/createalias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateAlias");
            //wsmodel.setApiname("CreateAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CreateAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletealias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteAlias");
            //wsmodel.setApiname("DeleteAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getalias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAlias");
            //wsmodel.setApiname("GetAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/checknationalityid")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CheckNationalityId(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CheckNationalityId Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CheckNationalityId");
            //wsmodel.setApiname("CheckNationalityId");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CheckNationalityId call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CheckNationalityId reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/checkemail")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CheckEmail(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CheckEmail Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CheckEmail");
            //wsmodel.setApiname("CheckEmail");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CheckEmail call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CheckEmail reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/checkusername")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CheckUserName(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CheckUserName Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CheckUserName");
            //wsmodel.setApiname("CheckUserName");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CheckUserName call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CheckUserName reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getregisteredcontacts")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRegisteredContacts(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRegisteredContacts Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRegisteredContacts");
            //wsmodel.setApiname("GetRegisteredContacts");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetRegisteredContacts call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRegisteredContacts reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getinvitecontacts")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetInviteContacts(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetInviteContacts Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetInviteContacts");
            //wsmodel.setApiname("GetInviteContacts");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetInviteContacts call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetInviteContacts reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getdrccontacts")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetDRCContacts(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetDRCContacts Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetDRCContacts");
            //wsmodel.setApiname("GetDRCContacts");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetDRCContacts call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetDRCContacts reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/createprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateWallet");
            //wsmodel.setApiname("CreateWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CreateWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updateprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateProfile(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateProfile Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateProfile");
            //wsmodel.setApiname("UpdateProfile");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateProfile call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateProfile reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/createwalletpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateWalletPIN(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateWalletPIN Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateWalletPIN");
            //wsmodel.setApiname("CreateWalletPIN");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CreateWalletPIN call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateWalletPIN reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/customerenablewalletaccount") //enable-disbale wallet account
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CustomerEnableWalletAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CustomerEnableWalletAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CustomerEnableWalletAccount");
            //wsmodel.setApiname("CustomerEnableWalletAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CustomerEnableWalletAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CustomerEnableWalletAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatewalletpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeWalletPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeWalletPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeWalletPin");
            //wsmodel.setApiname("ChangeWalletPin");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //Execute Thread Here...!
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeWalletPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeWalletPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/verifywalletpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity VerifyWalletPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource VerifyWalletPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("VerifyWalletPin");
            //wsmodel.setApiname("VerifyWalletPin");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //Execute Thread Here...!
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing VerifyWalletPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending VerifyWalletPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/requestdebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RequestDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DebitCardRequest Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DebitCardRequest");
            //wsmodel.setApiname("DebitCardRequest");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DebitCardRequest call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DebitCardRequest reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/enabledebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnableDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnableDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnableDebitCard");
            //wsmodel.setApiname("EnableDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnableDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnableDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/changedebitcardpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ChangeDebitCardPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ChangeDebitCardPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ChangeDebitCardPin");
            //wsmodel.setApiname("ChangeDebitCardPin");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ChangeDebitCardPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ChangeDebitCardPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/createwalletlevelone")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateWalletLevelOne(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateWalletLevelOne Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateWalletLevelOne");
            //wsmodel.setApiname("CreateWalletLevelOne");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing createwalletlevelone call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateWalletlevelone reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
        //return npwm;
    }

    @Path("/getbankdetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBankDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBankDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetBankDetails");
            //wsmodel.setApiname("GetBankDetails");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBankDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBankDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/linkaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LinkBankAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LinkBankAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("LinkBankAccount");
            //wsmodel.setApiname("LinkBankAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing LinkBankAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LinkBankAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmlinkaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmLinkAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmLinkAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("ConfirmLinkAccount");
            //wsmodel.setApiname("ConfirmLinkAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmLinkAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBankDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/unlinkaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UnLinkBankAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UnLinkBankAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("UnLinkBankAccount");
            //wsmodel.setApiname("UnLinkBankAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UnLinkBankAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UnLinkBankAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/linkbankaccountotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LinkBankAccountOTP(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LinkBankAccountOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("LinkBankAccountOTP");
            //wsmodel.setApiname("LinkBankAccountOTP");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing LinkBankAccountOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LinkBankAccountOTP reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getdebitcardaccountlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetDebitCardAccountList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetDebitCardAccountList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetDebitCardAccountList");
            //wsmodel.setApiname("GetDebitCardAccountList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetDebitCardAccountList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetDebitCardAccountList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/linkdebitcardaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LinkDebitCardAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LinkDebitCardAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("LinkDebitCardAccount");
            //wsmodel.setApiname("LinkDebitCardAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing LinkDebitCardAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LinkDebitCardAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/unlinkdebitcardaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UnLinkDebitCardAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UnLinkDebitCardAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("UnLinkDebitCardAccount");
            //wsmodel.setApiname("UnLinkDebitCardAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UnLinkDebitCardAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UnLinkDebitCardAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatelinkedaccountalias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateLinkedAccountAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateLinkedAccountAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("UpdateLinkedAccountAlias");
            //wsmodel.setApiname("UpdateLinkedAccountAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateLinkedAccountAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateLinkedAccountAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/setprimarylinkedaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetPrimaryLinkedAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetPrimaryLinkedAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("SetPrimaryLinkedAccount");
            //wsmodel.setApiname("SetPrimaryLinkedAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SetPrimaryLinkedAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetPrimaryLinkedAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getusertoken")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserToken(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserToken Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserToken");
            //wsmodel.setApiname("GetUserToken");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserToken call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserToken reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getuserwalletlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserWalletList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserWalletList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserWalletList");
            //wsmodel.setApiname("GetUserWalletList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserWalletList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserWalletList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getuserwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserWallet");
            //wsmodel.setApiname("GetUserWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getuserdebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserDebitCard");
            //wsmodel.setApiname("GetUserDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getuserlinkedaccountlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserLinkedAccountList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserLinkedAccountList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserLinkedAccountList");
            //wsmodel.setApiname("GetUserLinkedAccountList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserLinkedAccountList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserLinkedAccountList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/gettransactionlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserTransactionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserTransactionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetUserTransactionList");
            //wsmodel.setApiname("GetUserTransactionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserTransactionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/updatemerchantdashboard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMerchantDashboard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMerchantDashboard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("UpdateMerchantDashboard");
            //wsmodel.setApiname("UpdateMerchantDashboard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateMerchantDashboard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

//	@Path("/getcustomerbyMobileNumber")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity GetCustomerByMobileNumber(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//											   @HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//
//		try {
//			logger.info("WebResource GetCustomerByMobileNumber Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				return wsmodel;
//			}
//
//			wsmodel.setServicename("GetCustomerByMobileNumber");
//			//wsmodel.setApiname("UpdateMerchantDashboard");
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,"67f9db84fdd0362e4256e3b01906e3289fb13f27","97815c0eee2a987a27abc2ace4d00f5ccc389d28","Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
//			//MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			return ((Future<AppWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing GetUserTransactionList call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending UpdateMerchantDashboard reply [" + wsmodel.getRespcode() + "]");
//
//			return wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//		}
//	}

    @Path("/getbanktransactionlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBankTransactionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBankTransactionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetBankTransactionList");
            //wsmodel.setApiname("GetBankTransactionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBankTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBankTransactionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //m.rehman: for NayaPay, changing service name to sync switch, wallet and db
    //@Path("/loadwalletaccount")
    @Path("/loadwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LoadWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LoadWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("LoadWallet");
            wsmodel.setPaymentmethod("ACCOUNT");
            //wsmodel.setApiname("LoadWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing LoadWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LoadWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //m.rehman: for NayaPay, changing service name to sync switch, wallet and db
    //@Path("/unloadwalletaccount")
    @Path("/unloadwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UnloadWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UnloadWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UnloadWallet");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("UnloadWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UnloadWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UnloadWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoneyinquiry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoneyInquiry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyInquiry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyInquiry");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("SendMoneyInquiry");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendMoneyInquiry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyInquiry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("WalletTransaction");
            //wsmodel.setApiname("Send Money");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoneyalias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoneyAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyAlias");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("SendMoneyAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendMoneyAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/qrsendmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity QRSendMoney(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        String serviceName = TransactionCodes.QR_SEND_MONEY;

        logger.info("API {} | Request received", serviceName);
        if (wsmodel == null) {
            logger.error("API {} | No data received from request", serviceName);
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            wsmodel.setServicename(serviceName);
            wsmodel.setPaymentmethod("QRCODE");
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(
                    AppWSServer.id++,
                    wsmodel,
                    loginID,
                    loginPassword,
                    auth
            );
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", serviceName);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/banktowallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BankToWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BankToWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BankToWallet");
            //wsmodel.setApiname("BankToWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing BankToWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BankToWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/banktoalias")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BankToAlias(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BankToAlias Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BankToAlias");
            //wsmodel.setApiname("BankToAlias");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing BankToAlias call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BankToAlias reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/banktobank")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BankToBank(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BankToBank Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BankToBank");
            //wsmodel.setApiname("BankToBank");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing BankToBank call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BankToBank reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoneyenvoicash")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoneyEnvoiCash(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyEnvoiCash Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnvoiCashRequest");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Envoi Cash");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendMoneyEnvoiCash call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyEnvoiCash reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoneybankaccount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoneyBankAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyBankAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyBankAccount");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("SendMoneyBankAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendMoneyBankAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyBankAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getcountrylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCountryList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCountryList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCountryList");
            //wsmodel.setApiname("GetCountryList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCountryList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCountryList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getoccupationlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetOccupationList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetOccupationList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetOccupationList");
            wsmodel.setApiname("GetOccupationList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetOccupationList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetOccupationList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getmonthlyincomelist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMonthlyIncomeList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMonthlyIncomeList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMonthlyIncomeList");
            wsmodel.setApiname("GetMonthlyIncomeList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMonthlyIncomeList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMonthlyIncomeList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getmonthlyexpenditurelist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMonthlyExpenditureList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMonthlyExpenditureList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMonthlyExpenditureList");
            wsmodel.setApiname("GetMonthlyExpenditureList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMonthlyExpenditureList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMonthlyExpenditureList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getstatelist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetStateList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetStateList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetStateList");
            //wsmodel.setApiname("GetStateList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetStateList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetStateList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getcitylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCityList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCityList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCityList");
            //wsmodel.setApiname("GetCityList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCityList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCityList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmunicipalitylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMunicipalityList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMunicipalityList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMunicipalityList");
            //wsmodel.setApiname("GetMunicipalityList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMunicipalityList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMunicipalityList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getcallcodelistbycountry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCallCodeListbyCountry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCallCodeListbyCountry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCallCodeListbyCountry");
            //wsmodel.setApiname("GetCallCodeListbyCountry");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCallCodeListbyCountry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCallCodeListbyCountry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getcurrencylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCurrencyList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCurrencyList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCurrencyList");
            //wsmodel.setApiname("GetCurrencyList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCurrencyList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCurrencyList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //IMT update start
    @Path("/v2/getcountrylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRemitCountires(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRemitCountires Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitCountires");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitCountires call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitCountires reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getremitpayertypes")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRemitPayerTypes(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRemitPayerTypes Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitPayerTypes");
            //wsmodel.setApiname("GetRemitPayers");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitPayerTypes call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitPayerTypes reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getremitpayers")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRemitPayers(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRemitPayers Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitPayers");
            //wsmodel.setApiname("GetRemitPayers");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitPayers call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitPayers reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/getremitpayers")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetDynRemitPayers(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetDynRemitPayers Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitPayers");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("GetRemitPayers");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitPayers call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitPayers reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getremitpayerform")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRemitPayerForm(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRemitPayerForm Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitPayerForm");
            //wsmodel.setApiname("GetRemitPayers");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitPayerForm call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitPayerForm reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/intimt")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity InitSendMoneyInternational(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource InitSendMoneyInternational Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("InitSendMoneyInternational");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Send Money International");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing InitSendMoneyInternational call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending InitSendMoneyInternational reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendmoneyint")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendMoneyInternational(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyInternational Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyInternational");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Send Money International");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing SendMoneyInternational call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyInternational reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/sendmoneyint")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity NewSendMoneyInternational(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyInternational Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyInternational");
            wsmodel.setPaymentmethod("illicocash");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("Send Money International");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing SendMoneyInternational call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyInternational reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //IMT update end

    @Path("/requestmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RequestMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource RequestMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("RequestMoney");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Ask Money");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing RequestMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending RequestMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cancelrequestmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CancelRequestMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CancelRequestMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CancelRequestMoney");
            //wsmodel.setApiname("CancelAskMoney");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CancelRequestMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CancelRequestMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/rejectrequestmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RejectRequestMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource RejectRequestMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("RejectRequestMoney");
            //wsmodel.setApiname("RejectAskMoney");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing RejectRequestMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending RejectRequestMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmrequestmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmRequestMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmRequestMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ConfirmRequestMoney");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Confirm Ask Money");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmRequestMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmRequestMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getrequestmoneylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRequestMoneyList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRequestMoneyList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRequestMoneyList");
            //wsmodel.setApiname("GetAskMoneyList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetRequestMoneyList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRequestMoneyList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getenvoicashlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetEnvoiCashList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetEnvoiCashList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetEnvoiCashList");
            //wsmodel.setApiname("GetEnvoiCashList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetEnvoiCashList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetEnvoiCashList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cashoutrequest")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CashOutRequest(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CashOutRequest Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CashOutRequest");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("CashOutRequest");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CashOutRequest call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CashOutRequest reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cashout")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CashOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CashOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CashOut");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("CashOut");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CashOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CashOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cashoutnonillicorequest")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CashOutNonIllicoRequest(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CashOutNonIllicoRequest Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CashOutNonIllicoRequest");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("CashOutNonIllicoRequest");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CashOutNonIllicoRequest call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CashOutNonIllicoRequest reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cashoutnonillico")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CashOutNonIllico(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CashOutNonIllico Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CashOutNonIllico");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("CashOutNonIllico");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CashOutNonIllico call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CashOutNonIllico reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cancelcashout")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CancelCashOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CancelCashOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CancelCashOut");
            //wsmodel.setApiname("CancelCashOut");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CancelCashOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CancelCashOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getcashoutlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCashOutList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCashOutList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCashOutList");
            //wsmodel.setApiname("GetCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCashOutList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCashOutList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getunreadapprovalcount")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUnreadApprovalCount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUnreadApprovalCount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetUnreadApprovalCount");
            wsmodel.setApiname("GetUnreadApprovalCount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUnreadApprovalCount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUnreadApprovalCount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getoutgoingpendingremitloglist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetOutgoingPendingRemitLogList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetOutgoingPendingRemitLogList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetOutgoingPendingRemitLogList");
            //wsmodel.setApiname("GetOutgoingPendingRemitLogList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetOutgoingPendingRemitLogList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetOutgoingPendingRemitLogList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getincomingpendingremitloglist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetIncomingPendingRemitLogList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetIncomingPendingRemitLogList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetIncomingPendingRemitLogList");
            //wsmodel.setApiname("GetIncomingPendingRemitLogList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetIncomingPendingRemitLogList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetIncomingPendingRemitLogList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantbillertransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantBillerTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantBillerTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantBillerTransaction");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("MerchantBillerTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantBillerTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantBillerTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantbillercoretransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantBillerCoreTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantBillerCoreTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantBillerCoreTransaction");
            wsmodel.setPaymentmethod("ACCOUNT");
            //wsmodel.setApiname("MerchantBillerCoreTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantBillerCoreTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantBillerCoreTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantretailtransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantRetailTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantRetailTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantRetailTransaction");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("MerchantRetailTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantRetailTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantRetailTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantretailcoretransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantRetailCoreTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantRetailCoreTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantRetailCoreTransaction");
            wsmodel.setPaymentmethod("ACCOUNT");
            //wsmodel.setApiname("MerchantRetailCoreTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantRetailCoreTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantRetailTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmfraudotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmFraudOtp(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmFraudOtp Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ConfirmFraudOtp");
            //wsmodel.setApiname("ConfirmFraudOtp");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //WebServiceUtil.PrintWSMsg(wsmodel,true);


            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmFraudOtp call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmFraudOtp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmbankotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmBankOtp(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmBankOtp Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            //m.rehman: for NayaPay, changing service name to sync switch, wallet and db
            wsmodel.setServicename("ConfirmBankOtp");
            //wsmodel.setApiname("ConfirmBankOtp");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //WebServiceUtil.PrintWSMsg(wsmodel,true);


            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmBankOtp call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmBankOtp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    /// ////////////////////MERCHANT///////////////////////
    @Path("/addmerchantprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AddMerchantProfile(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AddMerchantProfile Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AddMerchantProfile");
            //wsmodel.setApiname("AddMerchantProfile");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AddMerchantProfile call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AddMerchantProfile reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchantprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantProfile(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantProfile Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantProfile");
            //wsmodel.setApiname("GetMerchantProfile");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantProfile call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantProfile reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatemerchantprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMerchantProfile(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMerchantProfile Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateMerchantProfile");
            //wsmodel.setApiname("UpdateMerchantProfile");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateMerchantProfile call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateMerchantProfile reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/enablemerchant")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnableMerchant(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnableMerchant Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnableMerchant");
            //wsmodel.setApiname("EnableMerchant");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnableMerchant call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnableMerchant reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/closemerchant")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CloseMerchant(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CloseMerchant Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CloseMerchant");
            //wsmodel.setApiname("CloseMerchant");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CloseMerchant call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CloseMerchant reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchanttransactionlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantTransactionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantTransactionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantTransactionList");
            //wsmodel.setApiname("GetMerchantTransactionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantTransactionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchanttransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantTransaction");
            //wsmodel.setApiname("GetMerchantTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/blockmerchant")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BlockMerchant(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BlockMerchant Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BlockMerchant");
            //wsmodel.setApiname("BlockMerchant");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BlockMerchant call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BlockMerchant reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/addmerchantrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AddMerchantRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AddMerchantRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AddMerchantRates");
            //wsmodel.setApiname("AddMerchantRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AddMerchantRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AddMerchantRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatemerchantrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMerchantRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMerchantRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateMerchantRates");
            //wsmodel.setApiname("UpdateMerchantRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateMerchantRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateMerchantRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchantrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantRates");
            //wsmodel.setApiname("GetMerchantRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/addmerchantcategory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AddMerchantCategory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AddMerchantCategory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AddMerchantCategory");
            //wsmodel.setApiname("AddMerchantCategory");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AddMerchantCategory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AddMerchantCategory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatemerchantcategory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMerchantCategory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMerchantCategory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateMerchantCategory");
            //wsmodel.setApiname("UpdateMerchantCategory");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateMerchantCategory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateMerchantCategory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletemerchantcategory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteMerchantCategory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteMerchantCategory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteMerchantCategory");
            //wsmodel.setApiname("DeleteMerchantCategory");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteMerchantCategory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteMerchantCategory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchantcategory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantCategory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantCategory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantCategory");
            //wsmodel.setApiname("GetMerchantCategory");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantCategory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantCategory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/addmerchantcategoryrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AddMerchantCategoryRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AddMerchantCategoryRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AddMerchantCategoryRates");
            //wsmodel.setApiname("AddMerchantCategoryRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AddMerchantCategoryRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AddMerchantCategoryRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatemerchantcategoryrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMerchantCategoryRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMerchantCategoryRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateMerchantCategoryRates");
            //wsmodel.setApiname("UpdateMerchantCategoryRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateMerchantCategoryRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateMerchantCategoryRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletemerchantcategoryrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteMerchantCategoryRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteMerchantCategoryRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteMerchantCategoryRates");
            //wsmodel.setApiname("DeleteMerchantCategoryRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteMerchantCategoryRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteMerchantCategoryRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getmerchantcategoryrates")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMerchantCategoryRates(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMerchantCategoryRates Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMerchantCategoryRates");
            //wsmodel.setApiname("GetMerchantCategoryRates");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMerchantCategoryRates call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMerchantCategoryRates reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //m.rehman: for NayaPay, adding new calls for document 2.0 <start>
    @Path("/merchantreversaltransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantReversalTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantReversalTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantReversalTransaction");
            //wsmodel.setApiname("MerchantReversalTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantReversalTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantReversalTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/blockmerchantparent")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BlockMerchantParent(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BlockMerchantParent Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BlockMerchantParent");
            //wsmodel.setApiname("BlockMerchantParent");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BlockMerchantParent call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BlockMerchantParent reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantsendmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantSendMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantSendMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantSendMoney");
            //wsmodel.setApiname("Merchant Send Money");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantSendMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantSendMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/merchantqrsendmoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantQRSendMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantQRSendMoney Request Received");
            //wsmodel.setQrid("merc999999999999589A85865B217A40F8E05301CFA8C0038600000000000042"); //TODO: Raza remove THIS
            //wsmodel.setQrid("merc999999999999789A7779ACC95E5D98E05301CFA8C0A89700000000000022"); //TODO: Raza remove THIS
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantQRSendMoney");
            wsmodel.setPaymentmethod("QRCODE");
            //wsmodel.setApiname("Merchant QR Send Money");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantQRSendMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantQRSendMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    /// ////////////////////MERCHANT///////////////////////

    @Path("/kycportal/fetchprovisionalwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity FetchProvisionalWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource FetchProvisionalWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("FetchProvisionalWallet");
            //wsmodel.setApiname("FetchProvisionalWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing FetchProvisionalWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending FetchProvisionalWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/kycportal/fetchprovisionalwalletlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity FetchProvisionalWalletList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource FetchProvisionalWalletList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("FetchProvisionalWalletList");
            //wsmodel.setApiname("FetchProvisionalWalletList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing FetchProvisionalWalletList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending FetchProvisionalWalletList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updateprovisionalwalletaddress")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateProvisionalWalletAddress(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateProvisionalWalletAddress Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateProvisionalWalletAddress");
            //wsmodel.setApiname("UpdateProvisionalWalletAddress");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateProvisionalWalletAddress call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateProvisionalWalletAddress reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/kycportal/activateprovisionalwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ActivateProvisionalWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ActivateProvisionalWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ActivateProvisionalWallet");
            //wsmodel.setApiname("ActivateProvisionalWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing ActivateProvisionalWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ActivateProvisionalWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/kycportal/deleteprovisionalwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteProvisionalWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteProvisionalWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteProvisionalWallet");
            //wsmodel.setApiname("DeleteProvisionalWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteProvisionalWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteProvisionalWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/verifyconsumer")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity VerifyConsumer(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource VerifyConsumer Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("VerifyConsumer");
            //wsmodel.setApiname("VerifyConsumer");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing VerifyConsumer call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending VerifyConsumer reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getconsumertransactions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetConsumerTransactions(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetConsumerTransactions Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetConsumerTransactions");
            //wsmodel.setApiname("GetConsumerTransactions");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetConsumerTransactions call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetConsumerTransactions reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/gettransactiondetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTransactionDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTransactionDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetTransactionDetails");
            //wsmodel.setApiname("GetTransactionDetails");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetTransactionDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetTransactionDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/getusertransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SupportPortalGetUserTransactionDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SupportPortalGetUserTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SupportPortalGetUserTransaction");
            //wsmodel.setApiname("SupportPortalGetUserTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SupportPortalGetUserTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SupportPortalGetUserTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getusertransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetUserTransaction");
            //wsmodel.setApiname("GetUserTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatewalletaddress")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateWalletAddress(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateWalletAddress Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateWalletAddress");
            //wsmodel.setApiname("UpdateWalletAddress");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateWalletAddress call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateWalletAddress reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatewalletsecondaryphonenumber")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateWalletSecondaryPhoneNumber(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateWalletSecondaryPhoneNumber Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateWalletSecondaryPhoneNumber");
            //wsmodel.setApiname("UpdateWalletSecondaryPhoneNumber");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateWalletSecondaryPhoneNumber call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateWalletSecondaryPhoneNumber reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/resetwalletpin")
    ///supportportal/resetwalletpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ResetWalletPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ResetWalletPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ResetWalletPin");
            //wsmodel.setApiname("ResetWalletPin");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ResetWalletPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ResetWalletPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/adminblockwalletaccount") //enable-disbale wallet account
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminBlockWalletAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminBlockWalletAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AdminBlockWalletAccount");
            //wsmodel.setApiname("AdminBlockWalletAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminBlockWalletAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminBlockWalletAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/activatedebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ActivateDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ActivateDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ActivateDebitCard");
            //wsmodel.setApiname("ActivateDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ActivateDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ActivateDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/activatedebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SupportPortalActivateDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SupportPortalActivateDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SupportPortalActivateDebitCard");
            //wsmodel.setApiname("SupportPortalActivateDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SupportPortalActivateDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SupportPortalActivateDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/enabledebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SupportPortalEnableDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SupportPortalEnableDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SupportPortalEnableDebitCard");
            //wsmodel.setApiname("SupportPortalEnableDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SupportPortalEnableDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SupportPortalEnableDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/blockdebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BlockDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BlockDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BlockDebitCard");
            //wsmodel.setApiname("BlockDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BlockDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BlockDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //m.rehman: for onelink bill inquiry
//	@Path("/onelinkbillinquiry")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public MWWsEntity OnelinkBillInquiry(MWWsEntity wsmodel) throws Exception {
//		try {
//			logger.info("WebResource OnelinkBillInquiry Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new MWWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//				return wsmodel;
//			}
//			wsmodel.setServicename("OnelinkBillInquiry");
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			return ((Future<MWWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing OnelinkBillInquiry call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending GetDebitCardStatus reply [" + wsmodel.getRespcode() + "]");
//			return wsmodel;
//		}
//	}

    //m.rehman: for onelink bill payment
//	@Path("/onelinkbillpayment")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public MWWsEntity OnelinkBillPayment(MWWsEntity wsmodel) throws Exception {
//		try {
//			logger.info("WebResource OnelinkBillPayment Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new MWWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//				return wsmodel;
//			}
//			wsmodel.setServicename("OnelinkBillPayment");
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			return ((Future<MWWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing OnelinkBillPayment call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending GetDebitCardStatus reply [" + wsmodel.getRespcode() + "]");
//			return wsmodel;
//		}
//	}


    @Path("/billinquiry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BillInquiry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BillInquiry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BillInquiry");
            //wsmodel.setApiname("BillInquiry");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BillInquiry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BillInquiry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getbillpackagelist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBillPackageList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBillPackageList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBillPackageList");
            //wsmodel.setApiname("GetBillPackageList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBillPackageList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBillPackageList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/billpayment")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BillPayment(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BillPayment Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BillPayment");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("BillPayment");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            wsmodel.setTrancurrency(null); //Raza REMOVE after testing
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BillPayment call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BillPayment reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/onelinkbillertransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity OnelinkBillerTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource OnelinkBillerTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("OnelinkBillerTransaction");
            //wsmodel.setApiname("OnelinkBillerTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing OnelinkBillerTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending OnelinkBillerTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/onelinkbillercoretransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity OnelinkBillerCoreTransaction(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource OnelinkBillerCoreTransaction Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("OnelinkBillerCoreTransaction");
            //wsmodel.setApiname("OnelinkBillerCoreTransaction");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing OnelinkBillerCoreTransaction call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending OnelinkBillerCoreTransaction reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/supportportal/getusertransactionlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SupportPortalGetUserTransactionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SupportPortalGetUserTransactionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("SupportPortalGetUserTransactionList");
            //wsmodel.setApiname("SupportPortalGetUserTransactionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SupportPortalGetUserTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SupportPortalGetUserTransactionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/getuserwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalGetUserWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalGetUserWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("AdminPortalGetUserWallet");
            //wsmodel.setApiname("AdminPortalGetUserWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalGetUserWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalGetUserWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/getuserdebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalGetUserDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalGetUserDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("AdminPortalGetUserDebitCard");
            //wsmodel.setApiname("AdminPortalGetUserDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalGetUserDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalGetUserDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/getuserlinkedaccountlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalGetUserLinkedAccountList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalGetUserLinkedAccountList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("AdminPortalGetUserLinkedAccountList");
            //wsmodel.setApiname("AdminPortalGetUserLinkedAccountList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalGetUserLinkedAccountList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalGetUserLinkedAccountList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/gettransaction")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalGetTransactionDetail(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalGetTransactionDetail Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("AdminPortalGetTransactionDetail");
            //wsmodel.setApiname("AdminPortalGetTransactionDetail");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalGetTransactionDetail call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalGetTransactionDetail reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/gettransactionlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalGetTransactionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalGetUserTransactionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("AdminPortalGetUserTransactionList");
            //wsmodel.setApiname("AdminPortalGetUserTransactionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalGetTransactionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalGetTransactionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adminportal/blockwalletaccount") //enable-disbale wallet account
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AdminPortalBlockWalletAccount(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AdminPortalBlockWalletAccount Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AdminPortalBlockWalletAccount");
            //wsmodel.setApiname("AdminPortalBlockWalletAccount");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AdminPortalBlockWalletAccount call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AdminPortalBlockWalletAccount reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/envelopload")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnvelopLoad(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnvelopLoad Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnvelopLoad");
            //wsmodel.setApiname("EnvelopLoad");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnvelopLoad call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnvelopLoad reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/envelopunload")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnvelopUnload(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnvelopUnload Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnvelopUnload");
            //wsmodel.setApiname("EnvelopUnload");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnvelopUnload call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnvelopUnload reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/reverseenvelop")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ReverseEnvelop(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ReverseEnvelop Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ReverseEnvelop");
            //wsmodel.setApiname("ReverseEnvelop");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ReverseEnvelop call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ReverseEnvelop reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/donation")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity Donation(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource Donation Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("Donation");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("Donation");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing Donation call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending Donation reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getsecretquestions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetKYCQuestionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetKYCQuestionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetKYCQuestionList");
            //wsmodel.setApiname("GetKYCQuestionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetKYCQuestionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetKYCQuestionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getusersecretquestions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetUserKYCQuestionList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetUserKYCQuestionList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetUserKYCQuestionList");
            //wsmodel.setApiname("GetUserKYCQuestionList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetUserKYCQuestionList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetUserKYCQuestionList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatesecretquestions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateSecretQuestions(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateSecretQuestions Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateWalletQuestions");
            //wsmodel.setApiname("UpdateWalletQuestions");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateSecretQuestions call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateSecretQuestions reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/verifysecretquestions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity VerifyUserSecretQuestion(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource VerifyUserSecretQuestion Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("VerifyUserSecretQuestion");
            //wsmodel.setApiname("VerifyUserSecretQuestion");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing VerifyUserSecretQuestion call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending VerifyUserSecretQuestion reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/customerinquiry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CustomerInquiry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CustomerInquiry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CustomerInquiry");
            //wsmodel.setApiname("CustomerInquiry");

            //TODO: Raza Remove Me
			/*if(!Util.hasText(wsmodel.getDestmobilenumber()))
			{
				wsmodel.setDestmobilenumber("00243893767829");
			}*/
            //TODO: Raza Remove Me


            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CustomerInquiry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CustomerInquiry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getidpictures")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetIdPictures(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetIdPictures Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetIdPictures");
            //wsmodel.setApiname("GetIdPictures");
            //TODO: Raza Remove Me
			/*if(!Util.hasText(wsmodel.getDestmobilenumber()))
			{
				wsmodel.setDestmobilenumber("00243893767829");
			}*/
            //TODO: Raza Remove Me


            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetIdPictures call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetIdPictures reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/customerinquiryforedit")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CustomerInquiryForEdit(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CustomerInquiryForEdit Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CustomerInquiryForEdit");
            //wsmodel.setApiname("CustomerInquiryForEdit");
            //TODO: Raza Remove Me
			/*if(!Util.hasText(wsmodel.getDestmobilenumber()))
			{
				wsmodel.setDestmobilenumber("00243893767829");
			}*/
            //TODO: Raza Remove Me


            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CustomerInquiryForEdit call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CustomerInquiryForEdit reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/merchantinquiry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MerchantInquiry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MerchantInquiry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MerchantInquiry");
            //wsmodel.setApiname("MerchantInquiry");
            //wsmodel.setQrid("merc999999999999589A85865B217A40F8E05301CFA8C0038600000000000042"); //TODO: Raza remove THIS
            //wsmodel.setQrid("merc999999999999789A7779ACC95E5D98E05301CFA8C0A89700000000000022"); //TODO: Raza remove THIS

            //TODO: Raza Remove Me
			/*if(!Util.hasText(wsmodel.getDestmobilenumber()))
			{
				wsmodel.setDestmobilenumber("00243893767829");
			}*/
            //TODO: Raza Remove Me


            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MerchantInquiry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MerchantInquiry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/supportportal/tempblockdebitcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity TempBlockDebitCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource TempBlockDebitCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("TempBlockDebitCard");
            //wsmodel.setApiname("TempBlockDebitCard");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing TempBlockDebitCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending TempBlockDebitCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/gettransactioncharge")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTransactionCharge(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTransactionCharge Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetTransactionCharge");
            //wsmodel.setApiname("GetTransactionCharge");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetTransactionCharge call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetTransactionCharge reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/createotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateOTP(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateOTP");
            //wsmodel.setApiname("CreateOTP");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;

        } catch (Exception e) {

            logger.error("Exception caught while executing CreateOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateOTP reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmOTP(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ConfirmOTP");
            //wsmodel.setApiname("ConfirmOTP");
            wsmodel.setIncomingip(request.getRemoteAddr());


//			logger.info("loginid" + loginid + "loginpassword" + loginpassword + "AUTH"+ auth);
//
//			if(Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals(SMSCategory.OTP_CONFIRMATION_UPDATEMOBILE)){
//				loginid = "67f9db84fdd0362e4256e3b01906e3289fb13f27";
//				loginpassword ="97815c0eee2a987a27abc2ace4d00f5ccc389d28";
//				auth = "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj";
//			}

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;

        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmOTP reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/forexgetrate")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ForexGetRate(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ForexGetRate Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ForexGetRate");
            //wsmodel.setApiname("ForexGetRate");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ForexGetRate call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ForexGetRate reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/forexpurchase")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ForexPurchase(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ForexPurchase Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ForexPurchase");
            //wsmodel.setApiname("ForexPurchase");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ForexPurchase call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ForexPurchase reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getappgraph")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAppGraph(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAppGraph Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAppGraph");
            //wsmodel.setApiname("GetAppGraph");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetAppGraph call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAppGraph reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getbankgraph")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBankGraph(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBankGraph Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBankGraph");
            //wsmodel.setApiname("GetBankGraph");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetBankGraph call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBankGraph reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/sendinvite")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendInvite(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendInvite Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendInvite");
            //wsmodel.setApiname("SendInvite");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing SendInvite call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendInvite reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getnotifications")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetNotifications(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetNotifications Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetNotifications");
            //wsmodel.setApiname("GetNotifications");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetNotifications call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetNotifications reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getacceptedinvites")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAcceptedInvites(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAcceptedInvites Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAcceptedInvites");
            //wsmodel.setApiname("GetAcceptedInvites");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetAcceptedInvites call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAcceptedInvites reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getcancelinvites")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetCancelInvites(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCancelInvites Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetCancelInvites");
            //wsmodel.setApiname("GetCancelInvites");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetCancelInvites call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetCancelInvites reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getsessionstate")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetSessionState(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetSessionState Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetSessionState");
            //wsmodel.setApiname("GetSessionState");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);


            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (!(wsmodel.getRespcode().equals(ISOResponseCodes.MW_APPROVED)) && !(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))) {
                wsmodel.setMobilenumber(null);
                wsmodel.setUsername(null);
            }

//			return ((Future<AppWsEntity>)executor.submit(process)).get();
            return wsmodel;


            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetSessionState call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetSessionState reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/setbeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetBeneficiary");
            //wsmodel.setApiname("SetBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing SetBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getbeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBeneficiaries(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBeneficiaries Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBeneficiaries");
            //wsmodel.setApiname("GetBeneficiaries");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBeneficiaries call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBeneficiaries reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletebeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteBeneficiary");
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //V2 start
    @Path("/v2/setbeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetDYNBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetBeneficiary");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("SetBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {
            logger.error("Exception caught while executing SetBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/getbeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetDYNBeneficiaries(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBeneficiaries Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBeneficiaries");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("GetBeneficiaries");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBeneficiaries call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBeneficiaries reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/deletebeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteDYNBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteDBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteBeneficiary");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    //V2 end

    @Path("/setlanguage")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetLanguage(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetLanguage Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetLanguage");
            //wsmodel.setApiname("SetLanguage");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing SetLanguage call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetLanguage reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/setnotiflanguage")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetNotifLanguage(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetNotifLanguage Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetNotifLanguage");
            //wsmodel.setApiname("SetNotifLanguage");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing SetNotifLanguage  call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetNotifLanguage  reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getbillpackagedetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBillPackageDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBillPackageDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBillPackageDetails");
            //wsmodel.setApiname("GetBillPackageDetails");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBillPackageDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBillPackageDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getaccountbalance")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAccountBalance(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAccountBalance Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAccountBalance");
            //wsmodel.setApiname("GetAccountBalance");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetAccountBalance call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAccountBalance reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    //TODO: Raza make seperate server as "Notification Server" and use it; not doing due to Time Limitation and Change Impact
    @Path("/sendsmsnotification")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendSMSNotification(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendSMSNotification Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("SendSMSNotification");
            //wsmodel.setApiname("SendSMSNotification");

            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendSMSNotification call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/redeemwallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RedeemWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource RedeemWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("RedeemWallet");
            //wsmodel.setApiname("SendSMSNotification");

            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing RedeemWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


//	@Path("/updatemerchantmobilenumber")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity UpdateMerchantMobileNumber(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//										  @HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//
//		try {
//			logger.info("WebResource UpdateMerchantMobileNumber Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				//return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
//				return wsmodel;
//			}
//			wsmodel.setServicename("UpdateMerchantMobileNumber");
//			//wsmodel.setApiname("SendSMSNotification");
//
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//create thread here
//			ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,"67f9db84fdd0362e4256e3b01906e3289fb13f27","97815c0eee2a987a27abc2ace4d00f5ccc389d28","Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
//			//MessageManager.threadPool.execute(process);
//			//return "SendSMSNotification Processed OK";
//			return ((Future<AppWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing UpdateMerchantMobileNumber call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//			//return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
//			return  wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//		}
//	}


//	@Path("/updatemerchantaccountnumber")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity UpdateMerchantAccountNumber(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//												   @HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//
//		try {
//			logger.info("WebResource UpdateMerchantAccountNumber Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				//return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
//				return wsmodel;
//			}
//			wsmodel.setServicename("UpdateMerchantAccountNumber");
//			//wsmodel.setApiname("SendSMSNotification");
//
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//create thread here
//			ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,"67f9db84fdd0362e4256e3b01906e3289fb13f27","97815c0eee2a987a27abc2ace4d00f5ccc389d28","Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
//			//MessageManager.threadPool.execute(process);
//			//return "SendSMSNotification Processed OK";
//			return ((Future<AppWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing UpdateMerchantAccountNumber call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//			//return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
//			return  wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//		}
//	}
//

    @Path("/updatemobilenumber")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateMobileNumber(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateMobileNumber Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("UpdateMobileNumber");
            //wsmodel.setApiname("SendSMSNotification");

            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateMobileNumber call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/verifymobilenumberforchange")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity VerifyMobileNumberForChange(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource VerifyMobileNumberForChange Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("VerifyMobileNumberForChange");
            //wsmodel.setApiname("SendSMSNotification");

            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing VerifyMobileNumberForChange call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/disablemobilenumberforchange")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DisableMobileNumberForChange(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DisableMobileNumberForChange Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DisableMobileNumberForChange");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DisableMobileNumberForChange call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/broadcastnotification")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BroadCastNotification(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BroadCastNotification Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "BroadCastNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("BroadCastNotification");
            //wsmodel.setApiname("BroadCastNotification");

            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "BroadCastNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BroadCastNotification call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "BroadCastNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatekycstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateKYCStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateKYCStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("UpdateKYCStatus");
            //wsmodel.setApiname("UpdateKYCStatus");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateKYCStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendsecretquestions")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SendSecretQuestions(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendSecretQuestions Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("SendSecretQuestions");
            //wsmodel.setApiname("SendSecretQuestions");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process;
            if (Util.hasText(loginid) || Util.hasText(loginpassword) || Util.hasText(auth)) {
                process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            } else {
                process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            }
            //MessageManager.threadPool.execute(process);
            //return "SendSecretQuestions Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SendSecretQuestions call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSecretQuestions Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatetermandcond")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateTermandCond(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateTermandCond Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("UpdateTermandCond");
            //wsmodel.setApiname("UpdateTermandCond");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateTermandCond call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/geterrordescription")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetErrorDescription(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetErrorDescription Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("GetErrorDescription");
            //wsmodel.setApiname("GetErrorDescription");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetErrorDescription call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getimtreasons")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetIMTReasons(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetIMTReasons Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("GetIMTReasons");
            //wsmodel.setApiname("GetIMTReasons");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetIMTReasons call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getrespcodeslist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetRespCodesList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRespCodesList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("GetRespCodesList");
            //wsmodel.setApiname("GetRespCodesList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetRespCodesList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/cscgetbalancedetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCGetBalanceDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCGetBalanceDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCGetBalanceDetails");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCGetBalanceDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCGetBalanceDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscgetamountdue")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCGetAmountDue(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCGetAmountDue Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCGetAmountDue");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCGetAmountDue call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCGetAmountDue reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscgetcards")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCGetCards(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCGetCardList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCGetCardList");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCGetCardList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCGetCardList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscgettransactionhistory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCGeTransactionHistory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCGetTransactionHistory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCGetTransactionHistory");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCGetTransactionHistory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCGetTransactionHistory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscchangecardstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCChangeCardStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCChangeCardStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCChangeCardStatus");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCChangeCardStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCChangeCardStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscgetclientdetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSCGetClientDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCGetClientDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCGetClientDetails");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCGetClientDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCGetClientDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/cscloadcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CSSLoadCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CSCLoadCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CSCLoadCard");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing CSCLoadCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CSCLoadCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    // Added by Affan on 26-July-23

    /////////////////////////////////////// CSC Calls End /////////////////////////////////////////////////


    /// ///////// S2M Calls Start

    @Path("/s2mgetcardlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MGetCardList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MGetCardList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MGetCardList");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MGetCardList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MGetCardList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/s2mchangecardstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MChangeCardStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MChangeCardStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MChangeCardStatus");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MChangeCardStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MChangeCardStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/s2mgetcardstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MGetCardStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MGetCardStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MGetCardStatus");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MGetCardStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MGetCardStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/s2mresetpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MResetPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MResetPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MResetPin");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MResetPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MResetPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/s2mgettransactionhistory")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MGetTransactionHistory(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MGetTransactionHistory Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MGetTransactionHistory");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MGetTransactionHistory call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MGetTransactionHistory reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/s2mresendpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity S2MResendPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource S2MResendPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("S2MResendPin");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            return wsmodel;
        } catch (Exception e) {

            logger.error("Exception caught while executing S2MResendPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending S2MResendPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


//	@Path("/s2mrequestchequebook")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity S2MRequestChequeBook(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//												@HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		try {
//			logger.info("WebResource S2MRequestChequeBook Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				return wsmodel;
//			}
//			wsmodel.setServicename("S2MRequestChequeBook");
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			((Future<AppWsEntity>)executor.submit(process)).get();
//			return wsmodel;
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing S2MRequestChequeBook call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending S2MRequestChequeBook reply [" + wsmodel.getRespcode() + "]");
//
//			return wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//			executor.shutdownNow();
//		}
//	}
//
//	@Path("/s2mgetbalance")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity S2MGetBalance(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//											@HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		try {
//			logger.info("WebResource S2MGetBalance Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				return wsmodel;
//			}
//			wsmodel.setServicename("S2MGetBalance");
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			((Future<AppWsEntity>)executor.submit(process)).get();
//			return wsmodel;
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing S2MGetBalance call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending S2MGetBalance reply [" + wsmodel.getRespcode() + "]");
//
//			return wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//			executor.shutdownNow();
//		}
//	}

//////////// S2M Calls End


    /// ///////// MPGS VCN start


    @Path("/testsopra")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity TestSopra(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource TestSopra Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("TestSopra");
            //wsmodel.setApiname("GetRespCodesList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing TestSopra call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    //Raza adding for NayaPay IPG Testing... remove me later... start //Raza 07-09-2022 using it for RAW Bank
    @Path("/mpgs")
    @POST
    @CrossOrigin
    public String MPGS(String PaReq) throws Exception {

        try {
            logger.info("WebResource for MPGS Received PaReq [" + PaReq + "], processing...");

            if (Util.hasText(PaReq)) {

                String orderid, transactionid, gatewayrecommendation, result;
                orderid = transactionid = gatewayrecommendation = result = "";

                String[] items = PaReq.split("&");

                if (items != null && items.length > 0) {
                    for (String item : items) {
                        logger.info("Item [" + item.substring(0, item.indexOf("=")) + "]");
                        logger.info("Item [" + item.substring(item.indexOf("=") + 1) + "]");

                        String ntemp = item.substring(0, item.indexOf("="));
                        String vtemp = item.substring(item.indexOf("=") + 1);

                        logger.info("ntemp [" + ntemp + "] vtemp [" + vtemp + "]");

                        if (ntemp.equals("order.id")) {
                            orderid = vtemp;
                        } else if (ntemp.equals("transaction.id")) {
                            transactionid = vtemp;
                        } else if (ntemp.equals("response.gatewayRecommendation")) {
                            gatewayrecommendation = vtemp;
                        } else if (ntemp.equals("result")) {
                            result = vtemp;
                        }
                    }
                }

                logger.info("OrderId [" + orderid + "] TransactionId [" + transactionid + "] GatewayRecommendation [" + gatewayrecommendation + "] Result [" + result + "]");

                if (Util.hasText(orderid) && Util.hasText(transactionid) && Util.hasText(gatewayrecommendation) && Util.hasText(result)) {

                    GeneralDao.Instance.beginTransaction();

                    AuthMPGSTransaction authtxn = new AuthMPGSTransaction();

                    authtxn.setOrigdata(PaReq);
                    authtxn.setOrderid(orderid);
                    authtxn.setTransactionid(transactionid);
                    authtxn.setGatewayrecommendation(gatewayrecommendation);
                    authtxn.setResult(result);

                    GeneralDao.Instance.save(authtxn);
                    GeneralDao.Instance.endTransaction();
                }

//				PaReq = PaReq.replace("%2","");
                PaReq = PaReq.replaceAll("%.", "");

                PaReq = PaReq.replace("MD=&PaRes=", "");
            }
            logger.info("PaReq [" + PaReq + "], returning...");

            return PaReq;
        } catch (Exception e) {

            logger.error("Exception caught while executing GetRespCodesList call");
            logger.error(WebServiceUtil.getStrException(e));

            if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
                GeneralDao.Instance.endTransaction();
            }

            return "Failed to Process MPGS request...";
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }

            if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
                GeneralDao.Instance.endTransaction();
            }
            //executor.shutdownNow();
        }
    }


    @Path("/getmpgsorderid/{orderid}")
    @GET
    public Response GetMPGSOrderId(@PathParam("orderid") String orderid) throws Exception {

        try {
            logger.info("WebResource for MPGS GetOrderId received for OrderId [" + orderid + "], processing...");

            if (Util.hasText(orderid)) {
                GeneralDao.Instance.beginTransaction();

                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + AuthMPGSTransaction.class.getName() + " c where c.orderid= :ORDER ";
                params = new HashMap<String, Object>();
                params.put("ORDER", orderid);

                AuthMPGSTransaction dbtxn = (AuthMPGSTransaction) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbtxn != null) {
                    if (Util.hasText(dbtxn.getGatewayrecommendation()) && Util.hasText(dbtxn.getResult())) {
                        if (dbtxn.getGatewayrecommendation().equals("PROCEED") && dbtxn.getResult().equals("SUCCESS")) {
                            return Response.status(200).entity("{\"respcode\": \"00\",\"respcodedesc\": \"Approved.\",\"status\":\"" + dbtxn.getResult() + "-" + dbtxn.getGatewayrecommendation() + "\"}").build();
                        } else {
                            logger.error("Invalid status found against OrderId [" + dbtxn.getOrderid() + "], rejecting...");
                            return Response.status(401).entity("{\"respcode\": \"95\",\"respcodedesc\": \"OrderId not Authorized.\",status\":\" " + dbtxn.getResult() + "-" + dbtxn.getGatewayrecommendation() + "\"}").build();
                        }
                    } else {
                        return Response.status(400).entity("{\"respcode\": \"128\",\"respcodedesc\": \"Invalid Order Id, Order Id not found\"}").build();
                    }
                } else {
                    logger.error("No MPGSAuthTxn found against OrderId [" + orderid + "], rejecting...");
                    return Response.status(400).entity("{\"respcode\": \"128\",\"respcodedesc\": \"Invalid Order Id, Order Id not found\"}").build();
                }
            }
            logger.info("OrderId not found, returning...");

            return Response.status(400).entity("{\"respcode\": \"128\",\"respcodedesc\": \"Invalid Order Id, Order Id not found\"}").build();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetMPGSOrderIdStatus call");
            logger.error(WebServiceUtil.getStrException(e));

            if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
                GeneralDao.Instance.endTransaction();
            }

            return Response.status(400).entity("{\"respcode\": \"46\",\"respcodedesc\": \"Unable to process\"}").build();
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            //executor.shutdownNow();

            if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
                GeneralDao.Instance.endTransaction();
            }
        }
    }
    //Raza adding for NayaPay IPG Testing... remove me later... end

    //Raza adding for Migration -- TODO: PLEASE REMOVE ME LATER start
    @Path("/migupdatebalance")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MigrateUpdatedBalance(AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MigrateUpdatedBalance Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
                return wsmodel;
            }
            wsmodel.setServicename("MigrateUpdatedBalance");
            wsmodel.setEncryptkey("gHcm4/La6Cs34fvA8SkfsrMD71LA6wSA9ewIYoMtraAFj0TDvzjb37Ok4W/DTtUC1hSr4WLilpzB+AKBkOEsOg8Yljrkp00NvXBrovojwLHuccdyRLa3KBe3O9vI/kuU");
            //wsmodel.setApiname("GetRespCodesList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, "67f9db84fdd0362e4256e3b01906e3289fb13f27", "97815c0eee2a987a27abc2ace4d00f5ccc389d28", "Basic OTVmYzYzODcxN2NjNTI3MTE5MTYzZTY3ODZiN2RmYjczNWRhYWI3MjpiZGZjZTQwNmM0MjcxMGZhNDgxMTBkMmYwN2Y0YTdlNGNhYTA3Zjdj");
            //MessageManager.threadPool.execute(process);
            //return "SendSMSNotification Processed OK";
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetRespCodesList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

            //return "SendSMSNotification Failed with [" + wsmodel.getRespcode() + "]";
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    //Raza adding for Migration -- TODO: PLEASE REMOVE ME LATER end

    //Raza adding for AgentPOS start
    @Path("/agentposcreatewallet")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AgentPOSCreateWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AgentPOSCreateWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AgentPOSCreateWallet");
            //wsmodel.setApiname("CreateWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing AgentPOSCreateWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AgentPOSCreateWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/agentposverifyotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AgentPOSVerifyOTP(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AgentPOSVerifyOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AgentPOSVerifyOTP");
            //wsmodel.setApiname("CreateWallet");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing AgentPOSVerifyOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AgentPOSVerifyOTP reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    //Raza adding for AgentPOS end


    /// Raza MPGS - LoadIllico start
    @Path("/getmpgscards")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMPGSCards(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMPGSCards Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMPGSCards");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetMPGSCards call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMPGSCards reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/initmpgsloadillico")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity InitMPGSLoadWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource InitMPGSLoadWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("InitMPGSLoadWallet");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing InitMPGSLoadWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending InitMPGSLoadWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/mpgsloadillico")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MPGSLoadWallet(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MPGSLoadWallet Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MPGSLoadWallet");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing MPGSLoadWallet call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MPGSLoadWallet reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/enablempgscard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnableMPGSCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnableMPGSCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnableMPGSCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnableMPGSCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnableMPGSCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/disablempgscard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DisbaleMPGSCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DisbaleMPGSCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DisbaleMPGSCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DisbaleMPGSCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DisbaleMPGSCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletempgscard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteMPGSCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteMPGSCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteMPGSCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteMPGSCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteMPGSCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/savempgscard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SaveMPGSCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SaveMPGSCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SaveMPGSCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SaveMPGSCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SaveMPGSCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    /// Raza MPGS - LoadIllico end


    //Raza MPGS VCN start
    @Path("/getvcncards")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetVCNCards(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetVCNCards Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetVCNCards");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetVCNCards call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetVCNCards reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/buyvcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BuyVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                  @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BuyVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BuyVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BuyVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BuyVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatevcnprofile")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateVCNProfile(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateVCNProfile Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateVCNProfile");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateVCNProfile call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateVCNProfile reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getvcncarddetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetVCNCardDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetVCNCardDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetVCNCardDetails");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetVCNCardDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetVCNCardDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/loadvcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity LoadVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LoadVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("LoadVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing LoadVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LoadVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/unloadvcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UnLoadVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UnLoadVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UnLoadVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UnLoadVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UnLoadVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/enablevcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity EnableVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource EnableVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("EnableVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing EnableVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending EnableVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/disablevcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DisableVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DisableVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DisableVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing DisableVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DisableVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getvcncardstatement")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetVCNCardStatement(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetVCNCardStatement Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetVCNCardStatement");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetVCNCardStatement call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetVCNCardStatement reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/resetvcncardpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ResetVCNCardPin(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ResetVCNCardPin Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ResetVCNCardPin");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ResetVCNCardPin call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ResetVCNCardPin reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getvcncardbalance")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetVCNCardBalance(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetVCNCardBalance Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetVCNCardBalance");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetVCNCardBalance call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetVCNCardBalance reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/activatevcncard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ActivateVCNCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ActivateVCNCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ActivateVCNCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ActivateVCNCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ActivateVCNCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getvcncardlimits")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetVCNCardLimits(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetVCNCardLimits Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetVCNCardLimits");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetVCNCardLimits call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetVCNCardLimits reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/testvcn")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity TestVCN(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource TestVCN Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("TestVCN");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing TestVCN call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending TestVCN reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


//	@Path("/getvcngiftcards")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity GetVCNGiftCards(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//								   @HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		try {
//			logger.info("WebResource GetVCNGiftCards Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				return wsmodel;
//			}
//			wsmodel.setServicename("GetVCNGiftCards");
//
//			wsmodel.setIncomingip(request.getRemoteAddr());
//
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			return ((Future<AppWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//
//			logger.error("Exception caught while executing GetVCNGiftCards call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending GetVCNGiftCards reply [" + wsmodel.getRespcode() + "]");
//
//			return wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//			executor.shutdownNow();
//		}
//	}


    @Path("/buyvcngiftcard")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BuyVCNGiftCard(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BuyVCNGiftCard Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BuyVCNGiftCard");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing BuyVCNGiftCard call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BuyVCNGiftCard reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    // Umer added for Updating CARD COLORS
    @Path("/updatevcncardcolor")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateVCNCardColor(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateVCNCardColor Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateVCNCardColor");

            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateVCNCardColor call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateVCNCardColor reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    //Raza MPGS VCN end


    @Path("/getencryptedpassword")
    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public String GetEncryptedPassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, @QueryParam("clear") String clear) {


        try {
            String encrypted = Base64.getEncoder().encodeToString(WSEncryptionUtil.DESEncryptCBCPKCS5Padding(clear, "7c7af5793727b8820ebf9b2a1914b4340381315bb408837169cdbacba4ad3e31"));

            if (Util.hasText(encrypted)) {
                return encrypted;
            } else {
                return "Failed to generate encryted password for input [" + clear + "]";
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            return "Exception caught while generating encryted password for input [" + clear + "]";
        }
    }


    @Path("/getdecryptedpassword")
    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public String GetDecryptedPassword(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, @QueryParam("encrypt") String encrypt) {


        try {
            logger.info("Encrypt [" + encrypt + "]");

            if (Util.hasText(encrypt) && encrypt.contains(" ")) {
                encrypt = encrypt.replace(" ", "+");
            }

            logger.info("Updated Encrypt [" + encrypt + "]");

            String decrypted = WSEncryptionUtil.DESDecryptCBCPKCS5Padding(Base64.getDecoder().decode(encrypt), "7c7af5793727b8820ebf9b2a1914b4340381315bb408837169cdbacba4ad3e31");

            if (Util.hasText(decrypted)) {
                return decrypted;
            } else {
                return "Failed to generate decrypted password for input [" + encrypt + "]";
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            return "Exception caught while generating encryted password for input [" + encrypt + "]";
        }
    }


    //App-Agent CashOut 15-12-2022 start
    @Path("/getagentenvoicashlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAgentEnvoiCashList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAgentEnvoiCashList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAgentEnvoiCashList");
            //wsmodel.setApiname("GetAgentEnvoiCashList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetAgentEnvoiCashList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAgentEnvoiCashList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/agentcashoutrequest")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AgentCashOutRequest(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AgentCashOutRequest Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AgentCashOutRequest");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("AgentCashOutRequest");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AgentCashOutRequest call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AgentCashOutRequest reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/agentenvoicashrequest")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AgentEnvoiCashRequest(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AgentEnvoiCashRequest Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AgentEnvoiCashRequest");
            wsmodel.setPaymentmethod("illicocash");
            //wsmodel.setApiname("AgentCashOutNonIllicoRequest");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AgentEnvoiCashRequest call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AgentEnvoiCashRequest reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/agentcancelcashout")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AgentCancelCashOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AgentCancelCashOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AgentCancelCashOut");
            //wsmodel.setApiname("AgentCancelCashOut");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AgentCancelCashOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AgentCancelCashOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getagentcashoutlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetAgentCashOutList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAgentCashOutList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAgentCashOutList");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetAgentCashOutList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAgentCashOutList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    //App-Agent CashOut 15-12-2022 end

    // Added by Affan on 2-NOV-23 ADC Start
    @Path("/createchannelpin")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateChannelPIN(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateChannelPIN Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateChannelPIN");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing CreateChannelPIN call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateChannelPIN reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    // Added by Affan on 2-NOV-23 ADC End


    //Device Binding start
    @Path("/getdeviceandiccidbindingstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetDeviceAndICCIDBindingStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetDeviceAndICCIDBindingStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetDeviceAndICCIDBindingStatus");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetDeviceAndICCIDBindingStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetDeviceAndICCIDBindingStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/toggledevicebindingstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ToggleDeviceBindingStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ToggleDeviceBindingStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ToggleDeviceBindingStatus");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ToggleDeviceBindingStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ToggleDeviceBindingStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/toggleiccbindingstatus")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ToggleICCBindingStatus(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ToggleICCBindingStatus Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ToggleICCBindingStatus");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing ToggleICCBindingStatus call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ToggleICCBindingStatus reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

//	@Path("/toggleiccidbindingstatus")
//	@POST
//@CrossOrigin
//	@Consumes({MediaType.APPLICATION_JSON})
//	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
//	public AppWsEntity ToggleICCIDBindingStatus(@HeaderParam("LogInName") String loginid,@HeaderParam("LogInPass") String loginpassword,
//												 @HeaderParam("Authorization") String auth,AppWsEntity wsmodel) throws Exception{
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		try {
//			logger.info("WebResource ToggleICCIDBindingStatus Request Received");
//			if(wsmodel == null)
//			{
//				logger.error("No Data Received from request! replying..");
//				wsmodel = new AppWsEntity();
//				wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//
//				return wsmodel;
//			}
//			wsmodel.setServicename("ToggleICCIDBindingStatus");
//			//wsmodel.setApiname("GetAgentCashOutList");
//			//m.rehman: for validating incoming ip address
//			wsmodel.setIncomingip(request.getRemoteAddr());
//			//ExecutorService executor = MessageManager.threadPool;
//			MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++,wsmodel,loginid,loginpassword,auth);
//			return ((Future<AppWsEntity>)executor.submit(process)).get();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace()
//			logger.error("Exception caught while executing ToggleICCIDBindingStatus call");
//			logger.error(WebServiceUtil.getStrException(e));
//			wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//			logger.error("Sending ToggleICCIDBindingStatus reply [" + wsmodel.getRespcode() + "]");
//
//			return wsmodel;
//		}
//		finally {
//			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
//			if (session != null) {
//				session.invalidate();
//			}
//			executor.shutdownNow();
//		}
//	}

    @Path("/getbinddevices")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBindDevices(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBindDevices Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBindDevices");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBindDevices call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBindDevices reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/adddeviceforbinding")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity AddDeviceForBinding(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource AddDeviceForBinding Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("AddDeviceForBinding");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing AddDeviceForBinding call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending AddDeviceForBinding reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/removedevicefrombinding")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity RemoveDeviceFromBinding(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource RemoveDeviceFromBinding Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("RemoveDeviceFromBinding");
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing RemoveDeviceFromBinding call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending RemoveDeviceFromBinding reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
    //Device Binding end

    // Muhammad Hamza -- MOBILE BANKING Features -- 30-MAY-2024  -- START

    @Path("/getaccountopposition")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetAccountOpposition(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAccountOpposition Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAccountOpposition");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing GetAccountOpposition call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAccountOpposition reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getaccountrib")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetAccountRIB(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetAccountRIB Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetAccountRIB");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing GetAccountRIB call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetAccountRIB reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/sendrmemail")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity ContactRM(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ContactRM Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ContactRM");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing ContactRM call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ContactRM reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getsubjectlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetSubjectList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetSubjectList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetSubjectList");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing GetSubjectList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetSubjectList reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/gettransactionreasons")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity TransactionReasons(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource TransactionReasons Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("TransactionReasons");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing TransactionReasons call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending TransactionReasons reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getrmemails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetRMEmails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRMEmails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRMEmails");
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();


        } catch (Exception e) {

            logger.error("Exception caught while executing GetRMEmails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRMEmails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/createemailotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity CreateEmailOTP(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateEmailOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
            wsmodel.setServicename("CreateEmailOTP");
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;
        } catch (Exception e) {
            logger.error("Exception caught while executing CreateEmailOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateEmailOTP reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirmemailotp")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity ConfirmEmailOTP(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmEmailOTP Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
            wsmodel.setServicename("ConfirmEmailOTP");
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();

            wsmodel.setOtp(null);
            return wsmodel;
        } catch (Exception e) {
            logger.error("Exception caught while executing ConfirmEmailOTP call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmEmailOTP reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/titlefetch")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity getTitleFetch(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (wsmodel == null) {
            logger.error("No Data Received from request! replying..");
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            logger.info("WebResource TitleFetch Request Received");
            wsmodel.setServicename("TitleFetch");
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();

            wsmodel.setOtp(null);
            return wsmodel;
        } catch (Exception e) {
            logger.error("Exception caught while executing TitleFetch call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmEmailOTP reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/setotpchannel")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetOTPChannel(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetOTPChannel Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetOTPChannel");
            //wsmodel.setApiname("SetOTPChannel");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            //return ((Future<AppWsEntity>)executor.submit(process)).get();
            ((Future<AppWsEntity>) executor.submit(process)).get();
            wsmodel.setOtp(null);
            return wsmodel;

        } catch (Exception e) {
            //
            logger.error("Exception caught while executing SetOTPChannel call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetOTPChannel reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/createbeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity CreateBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource CreateBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("CreateBeneficiary");
            //wsmodel.setApiname("SetBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing CreateBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending CreateBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getbeneficiarieslist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBeneficiariesList(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBeneficiariesList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBeneficiariesList");
            //wsmodel.setApiname("GetBeneficiaries");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBeneficiariesList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending Length reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/deletebeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteBeneficiaries(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteBeneficiaries Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteBeneficiaries");
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteBeneficiaries call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteBeneficiaries reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/updatebeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity UpdateBeneficiaries(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource UpdateBeneficiaries Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("UpdateBeneficiaries");
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing UpdateBeneficiaries call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending UpdateBeneficiaries reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/beneficiariesdetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BeneficiariesDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                            @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BeneficiariesDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BeneficiariesDetails");
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing BeneficiariesDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BeneficiariesDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/gettransactiontypes")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTransactionTypes(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTransactionTypes Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetTransactionTypes");
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetTransactionTypes call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetTransactionTypes reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/bankforexpurchase")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity BankForexPurchase(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource BankForexPurchase Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("BankForexPurchase");
            //wsmodel.setApiname("ForexPurchase");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing BankForexPurchase call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending BankForexPurchase reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/transfermoney")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity TransferMoney(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource TransferMoney Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("TransferMoney");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing TransferMoney call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending TransferMoney reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getbankcodes")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetBankCodes(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBankCodes Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBankCodes");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetBankCodes call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBankCodes reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    // Muhammad Hamza -- MOBILE BANKING Features -- 30-MAY-2024  -- END


    //Muhammad Hamza -- E-Ticketing -- 06-Sep-2024 --  Start
    @Path("/getservices")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServices(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServices Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServices");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetServices call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServices reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getservicestags")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServicesTags(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServicesTags Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServicesTags");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetServicesTags call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServicesTags reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getservicedetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServiceDetails(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServiceDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServiceDetails");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetServiceDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServiceDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/geteventinvoice")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetEventInvoice(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetEventInvoice Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetEventInvoice");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetEventInvoice call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetEventInvoice reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/confirminvoicepayment")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmInvoicePayment(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmInvoicePayment Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ConfirmInvoicePayment");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmInvoicePayment call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmInvoicePayment reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getinvoicedetail")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetInvoiceDetail(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetInvoiceDetail Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetInvoiceDetail");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetInvoiceDetail call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetInvoiceDetail reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //For Eticketing V3

    @Path("/v2/getticketlist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTicketListV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTicketList Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
            wsmodel.setServicename("GetTicketList");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {
            logger.error("Exception caught while executing GetTicketList call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetTicketList reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v2/getservices")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServicesV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServices Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServices");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {
            logger.error("Exception caught while executing GetServices call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServices reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v2/getservicestags")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServicesTagsV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServicesTags Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServicesTags");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {
            logger.error("Exception caught while executing GetServicesTags call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServicesTags reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v2/getservicedetails")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetServiceDetailsV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetServiceDetails Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetServiceDetails");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetServiceDetails call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServiceDetails reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/geteventinvoice")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetEventInvoiceV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetEventInvoice Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetEventInvoice");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetEventInvoice call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetEventInvoice reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/confirminvoicepayment")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity ConfirmInvoicePaymentV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource ConfirmInvoicePayment Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("ConfirmInvoicePayment");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing ConfirmInvoicePayment call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending ConfirmInvoicePayment reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v2/getinvoicedetail")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetInvoiceDetailV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetInvoiceDetail Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetInvoiceDetail");
            wsmodel.setApiversion(APIVersion.VERSION2);
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetInvoiceDetail call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetInvoiceDetail reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //For Eticketing V2


    //Muhammad Hamza -- MNO - WalletToWallet -- 14-OCT-2024 --  START

    @Path("/getmnos")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetMNOs(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                               @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetMNOs Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetMNOs");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {
//
            logger.error("Exception caught while executing GetMNOs call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetMNOs reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/gettitlefetchout")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTitleFetchOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTitleFetchOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetTitleFetchOut");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {
//
            logger.error("Exception caught while executing GetTitleFetchOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetTitleFetchOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/mnotransfer")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity MNOTransferOut(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                      @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource MNOTransferOut Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("MNOTransferOut");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing MNOTransferOut call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending MNOTransferOut reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    //Muhammad Hamza -- MNO - WalletToWallet -- 14-OCT-2024 --  END
    //M. NAVEED 19-06-2025 THALESD1 SDK TOKEN TAP & PAY

    @Path("/d1sdkaccesstoken")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity D1GetAccessToken(@HeaderParam("LogInName") String loginid,
                                        @HeaderParam("LogInPass") String loginpassword,
                                        @HeaderParam("Authorization") String auth,
                                        AppWsEntity wsmodel) throws Exception {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            logger.info("WebResource D1SDKAccessToken Request Received");

            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
            if (Util.hasText(wsmodel.getMobilenumber())) {

                wsmodel.setServicename("D1SDKAccessToken");
                wsmodel.setIncomingip(request.getRemoteAddr());

                MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
                return ((Future<AppWsEntity>) executor.submit(process)).get();
            } else {
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

        } catch (Exception e) {
            logger.error("Exception caught while executing D1SDKAccessToken call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            executor.shutdownNow();
        }
    }

    @Path("/getcardid")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetCardId(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetCardId Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }

            wsmodel.setServicename("GetCardId");
            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing GetCardId call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetServices reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    /// /Hamza adding for Incoming Money Gram Calls - End - 16122024

    @Path("/incomingremittanceinquiry")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity IncomingRemittanceInquiry(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                 @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource IncomingRemittanceInquiry Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            //wsmodel.setServicename("IncomingRemittanceInquiry");
            wsmodel.setServicename("BenificiaryInquiry");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing IncomingRemittanceInquiry call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending IncomingRemittanceInquiry reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/incomingremittance")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity IncomingRemittance(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                          @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource IncomingRemittance Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("IncomingRemittance");
            //wsmodel.setServicename("SendRemittance");
            //wsmodel.setApiname("QuickSendMoney");
            wsmodel.setIncomingip(request.getRemoteAddr());

            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing IncomingRemittance call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending IncomingRemittance reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    // Hamza adding for Incoming Money Gram Calls - Start - 16122024


    //Hamza Adding for Outgoing MoneyGram Calls - Start  - 04042025

    @Path("/v3/getcountrylist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetV3RemitCountires(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetRemitCountires Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitCountires");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("GetAgentCashOutList");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitCountires call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitCountires reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v3/getremitpayers")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetV3DynRemitPayers(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetDynRemitPayers Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetRemitPayers");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("GetRemitPayers");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing GetRemitPayers call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetRemitPayers reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v3/intimt")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity InitSendMoneyInternationalV3(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                    @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource InitSendMoneyInternational Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("InitSendMoneyInternational");
            wsmodel.setPaymentmethod("illicocash");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("Send Money International");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing InitSendMoneyInternational call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending InitSendMoneyInternational reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v3/sendmoneyint")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity NewV3SendMoneyInternational(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                                   @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SendMoneyInternational Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SendMoneyInternational");
            wsmodel.setPaymentmethod("illicocash");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("Send Money International");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();
            if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        } catch (Exception e) {
            //
            logger.error("Exception caught while executing SendMoneyInternational call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SendMoneyInternational reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/v3/setbeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SetV3DYNBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                           @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SetBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("SetBeneficiary");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("SetBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {
            logger.error("Exception caught while executing SetBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SetBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v3/getbeneficiaries")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetV3DYNBeneficiaries(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                             @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetBeneficiaries Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetBeneficiaries");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("GetBeneficiaries");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing GetBeneficiaries call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending GetBeneficiaries reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/v3/deletebeneficiary")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeleteV3DYNBeneficiary(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                              @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeleteDBeneficiary Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeleteBeneficiary");
            wsmodel.setApiversion(APIVersion.VERSION3);
            //wsmodel.setApiname("DeleteBeneficiary");
            //m.rehman: for validating incoming ip address
            wsmodel.setIncomingip(request.getRemoteAddr());
            //create thread here
            //ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
            //MessageManager.threadPool.execute(process);
        } catch (Exception e) {

            logger.error("Exception caught while executing DeleteBeneficiary call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeleteBeneficiary reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //Hamza Adding for Outgoing MoneyGram Calls - End  - 04042025

    //Naveed adding for ID-WISE Calls - Start  - 12092025
    @Path("/v2/signup")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity SignUpV2(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SignUpV2 Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename(TransactionCodes.CREATE_WALLET);
            wsmodel.setApiversion("v2");
            //wsmodel.setApiname("SignUp");
            wsmodel.setIncomingip(request.getRemoteAddr());

            ////ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SignUpV2 call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SignUp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/gettrackingid")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetTrackingId(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                     @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource GetTrackingId Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetTrackingId");
            wsmodel.setIncomingip(request.getRemoteAddr());

            ////ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing GetTrackingId call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SignUp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }


    @Path("/getsignupformmeta")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity GetSignUpFormMeta(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                         @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource SignUp Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("GetSignUpFormMeta"); //Raza executing as SignUp
            //wsmodel.setApiname("SignUp");
            wsmodel.setIncomingip(request.getRemoteAddr());

            ////ExecutorService executor = MessageManager.threadPool;
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();
        } catch (Exception e) {

            logger.error("Exception caught while executing SignUp call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending SignUp reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    //Naveed adding for ID-WISE Calls - END  - 12092025

    //Nofel Adding for Device Unbinding - Start - 17092025
    @Path("/deviceunbinding")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity DeviceUnbinding(@HeaderParam("LogInName") String loginid, @HeaderParam("LogInPass") String loginpassword,
                                       @HeaderParam("Authorization") String auth, AppWsEntity wsmodel) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource DeviceUnbinding Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("DeviceUnbinding");
            wsmodel.setIncomingip(request.getRemoteAddr());

            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            return ((Future<AppWsEntity>) executor.submit(process)).get();

        } catch (Exception e) {

            logger.error("Exception caught while executing DeviceUnbinding call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending DeviceUnbinding reply [" + wsmodel.getRespcode() + "]");

            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/privacypolicy")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public AppWsEntity getPrivacyPolicy(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            logger.info("WebResource LogIn Request Received");
            if (wsmodel == null) {
                logger.error("No Data Received from request! replying..");
                wsmodel = new AppWsEntity();
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

                return wsmodel;
            }
            wsmodel.setServicename("PrivacyPolicy");
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            ((Future<AppWsEntity>) executor.submit(process)).get();

            return wsmodel;
        } catch (Exception e) {
            logger.error("Exception caught while executing LogIn call");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Sending LogIn reply [" + wsmodel.getRespcode() + "]");
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/softdeleteuser")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity softDeleteUser(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        logger.info("API Path: {}", request.getPathInfo());

        String serviceName = TransactionCodes.SOFT_DELETE_USER;

        if (wsmodel == null) {
            logger.error("API {} | No data received from request", serviceName);
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            logger.info("API {} | Request received", serviceName);
            wsmodel.setServicename(serviceName);
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            executor.submit(process).get();
            return wsmodel;
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", serviceName);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getallusers")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity getAllUsers(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        logger.info("API Path: {}", request.getPathInfo());

        String serviceName = TransactionCodes.GET_ALL_USERS;

        if (wsmodel == null) {
            logger.error("API {} | No data received from request", serviceName);
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            logger.info("API {} | Request received", serviceName);
            wsmodel.setServicename(serviceName);
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            executor.submit(process).get();
            return wsmodel;
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", serviceName);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getenvvariables")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity getEnvVariables(
            @HeaderParam("LogInName") String loginid,
            @HeaderParam("LogInPass") String loginpassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        logger.info("API Path: {}", request.getPathInfo());

        String serviceName = TransactionCodes.GET_ENV_VARS;

        if (wsmodel == null) {
            logger.error("API {} | No data received from request", serviceName);
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            logger.info("API {} | Request received", serviceName);
            wsmodel.setServicename(serviceName);
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginid, loginpassword, auth);
            executor.submit(process).get();
            return wsmodel;
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", serviceName);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }

    @Path("/getbankslist")
    @POST
    @CrossOrigin
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppWsEntity GetServicesList(
            @HeaderParam("LogInName") String loginID,
            @HeaderParam("LogInPass") String loginPassword,
            @HeaderParam("Authorization") String auth,
            AppWsEntity wsmodel
    ) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        String serviceName = TransactionCodes.GET_BANKS_LIST;

        logger.info("API {} | Request received", serviceName);

        if (wsmodel == null) {
            logger.error("API {} | No data received from request", serviceName);
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        try {
            wsmodel.setServicename(serviceName);
            wsmodel.setIncomingip(request.getRemoteAddr());
            MainAppWSProcess process = new MainAppWSProcess(AppWSServer.id++, wsmodel, loginID, loginPassword, auth);
            return executor.submit(process).get();
        } catch (Exception e) {
            logger.error("API {} | Exception while executing request", serviceName);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            executor.shutdownNow();
        }
    }
}
