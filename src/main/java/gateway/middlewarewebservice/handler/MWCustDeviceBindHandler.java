package gateway.middlewarewebservice.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import pk.vaulsys.apigateway.base.config.*;
import pk.vaulsys.apigateway.customer.*;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.base.CustomerType;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWWSOperation;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.DeviceObj;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import java.util.*;

public class MWCustDeviceBindHandler {
    private static final Logger logger = LogManager.getLogger(MWCustDeviceBindHandler.class);


    public static AppWsEntity processGetDeviceAndICCIDBindingStatusRequest(AppWsEntity wsmodel)
    {
        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            wsmodel.setIccidstatus((customer.getIccidbindingenabled()) ? "true" : "false");
            wsmodel.setDevicebindstatus((customer.getDevicebindingenabled()) ? "true" : "false");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

    public static AppWsEntity processToggleDeviceBindingStatusRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            if(!Util.hasText(wsmodel.getDevicebindstatus()))
            {
                logger.error("DeviceBindStatus [" + wsmodel.getDevicebindstatus() + "] not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }
            else if(Util.hasText(wsmodel.getDevicebindstatus()) && !wsmodel.getDevicebindstatus().equals("true") && !wsmodel.getDevicebindstatus().equals("false")){
                logger.error("Invalid DeviceBindStatus status [" + wsmodel.getDevicebindstatus() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            if(wsmodel.getDevicebindstatus().equals("true")){
                if(customer.getDevicebindingenabled()){
                    logger.error("Customer Device Bind status already enabled, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    return wsmodel;
                }

                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                MWDeviceLog device = (MWDeviceLog)GeneralDao.Instance.findObject(dbQuery, params);

                if(device == null){
                    logger.error("Failed to get Device against Id [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    return wsmodel;
                }

                if(BindCustomerDevice(customer, device, MWCustDevcBindReasons.MANUAL)){
                    customer.setDevicebindingenabled(true);
                }
                else{
                    logger.error("Failed to Bind Device [" + wsmodel.getSecurityparams().getDeviceid() + "] with Customer [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    return wsmodel;
                }

            }
            else {
                if(!customer.getDevicebindingenabled()){
                    logger.error("Customer Device Bind status already disabled, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    return wsmodel;
                }
                customer.setDevicebindingenabled(false);
            }
            GeneralDao.Instance.saveOrUpdate(customer);

            wsmodel.setDevicebindstatus(customer.getDevicebindingenabled().toString());
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

    public static AppWsEntity processToggleICCBindingStatusRequest(AppWsEntity wsmodel)
    {
        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            if(!Util.hasText(wsmodel.getIccidstatus()))
            {
                logger.error("ICCID status [" + wsmodel.getIccidstatus() + "] not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }
            else if(Util.hasText(wsmodel.getIccidstatus()) && !wsmodel.getIccidstatus().equals("true") && !wsmodel.getIccidstatus().equals("false")){
                logger.error("Invalid ICCID status [" + wsmodel.getIccidstatus() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }


            if(Util.hasText(wsmodel.getIccidstatus())){
                if(wsmodel.getIccidstatus().equals("true")){
                    if(customer.getIccidbindingenabled()){
                        logger.error("Customer ICCID status already enabled, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return wsmodel;
                    }
                    customer.setIccid(wsmodel.getSecurityparams().getIccid());
                    customer.setIccidbindingenabled(true);
                }
                else {
                    if(!customer.getIccidbindingenabled()){
                        logger.error("Customer ICCID status already disabled, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return wsmodel;
                    }
                    customer.setIccidbindingenabled(false);
                }
                GeneralDao.Instance.saveOrUpdate(customer);
            }


            wsmodel.setIccidstatus(customer.getIccidbindingenabled().toString());
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

//    public static AppWsEntity processToggleDeviceAndICCBindingStatusRequest(AppWsEntity wsmodel)
//    {
//        logger.info("Validating Session...");
//        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
//        {
//            logger.error("Failed to Validate Session, rejecting...");
//            return wsmodel;
//        }
//
//        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());
//
//        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {
//
//            if(!Util.hasText(wsmodel.getIccidstatus()) && !Util.hasText(wsmodel.getDevicebindstatus()))
//            {
//                logger.error("ICCID status [" + wsmodel.getIccidstatus() + "] and DeviceBindStatus [" + wsmodel.getDevicebindstatus() + "] not found in request, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//                return wsmodel;
//            }
//            else if(Util.hasText(wsmodel.getIccidstatus()) && !wsmodel.getIccidstatus().equals("true") && !wsmodel.getIccidstatus().equals("false")){
//                logger.error("Invalid ICCID status [" + wsmodel.getIccidstatus() + "], rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//                return wsmodel;
//            }
//            else if(Util.hasText(wsmodel.getDevicebindstatus()) && !wsmodel.getDevicebindstatus().equals("true") && !wsmodel.getDevicebindstatus().equals("false")){
//                logger.error("Invalid DeviceBindStatus status [" + wsmodel.getDevicebindstatus() + "], rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//                return wsmodel;
//            }
//
//            if(Util.hasText(wsmodel.getIccidstatus())){
//                    if(wsmodel.getIccidstatus().equals("true")){
//                        if(customer.getIccidbindingenabled()){
//                            logger.error("Customer ICCID status already enabled, rejecting...");
//                            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                            return wsmodel;
//                        }
//                     customer.setIccidbindingenabled(true);
//                    }
//                    else {
//                        if(!customer.getIccidbindingenabled()){
//                            logger.error("Customer ICCID status already disabled, rejecting...");
//                            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                            return wsmodel;
//                        }
//                        customer.setIccidbindingenabled(false);
//                    }
//                    GeneralDao.Instance.saveOrUpdate(customer);
//            }
//
//            if(Util.hasText(wsmodel.getDevicebindstatus())){
//                if(wsmodel.getDevicebindstatus().equals("true")){
//                    if(customer.getDevicebindingenabled()){
//                        logger.error("Customer Device Bind status already enabled, rejecting...");
//                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                        return wsmodel;
//                    }
//                    customer.setDevicebindingenabled(true);
//                }
//                else {
//                    if(!customer.getDevicebindingenabled()){
//                        logger.error("Customer Device Bind status already disabled, rejecting...");
//                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                        return wsmodel;
//                    }
//                    customer.setDevicebindingenabled(false);
//                }
//                GeneralDao.Instance.saveOrUpdate(customer);
//            }
//
//
//            wsmodel.setIccidstatus(customer.getIccidbindingenabled().toString());
//            wsmodel.setDevicebindstatus(customer.getDevicebindingenabled().toString());
//            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//            return wsmodel;
//        }
//        else{
//            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
//            return wsmodel;
//        }
//    }

    public static AppWsEntity processGetBindDevicesRequest(AppWsEntity wsmodel)
    {
        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            if(customer.getDevicebindingenabled()){

                String dbQuery;
                Map<String, Object> params;
                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.customer= :CUST and c.status = :STATUS ";
                params = new HashMap<String, Object>();
                params.put("CUST", customer);
                params.put("STATUS", MWCustDevcBindStatus.ACTIVE);

                List<MWCustDeviceBindingLog> dbbindlog = GeneralDao.Instance.find(dbQuery, params);

                if(dbbindlog != null && dbbindlog.size() > 0){
                    List<DeviceObj> devclist = new ArrayList<>();
                    for(MWCustDeviceBindingLog d : dbbindlog){
                        DeviceObj obj = new DeviceObj();
                        obj.setBaseintegrityflag(d.getDevice().getBaseintegrity().toString());
                        obj.setCtsprofileflag(d.getDevice().getCtsprofile().toString());
                        obj.setDeviceid(d.getDevice().getDeviceid());
                        obj.setDevicemodel(d.getDevice().getDevicemodel());
                        obj.setIccid(d.getDevice().getIccid());
                        obj.setImei(d.getDevice().getImei());
                        obj.setOperatingsystem(d.getDevice().getOperatingsystem());
                        obj.setRootedflag(d.getDevice().getIsrooted().toString());
                        obj.setScreenresolution(d.getDevice().getScreenresolution());
                        obj.setStatus(d.getStatus());
                        obj.setUuid(d.getDevice().getUuid());
                        devclist.add(obj);
                    }

                    wsmodel.setDevices(devclist);
                }
                else{
                    logger.error("No Device Binding Log record found for customer [" + customer.getMobilenumber() + "]");
                }
            }
            else{
                logger.error("Customer Device Binding disabled!");
            }

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

    public static AppWsEntity processAddDeviceForBindingRequest(AppWsEntity wsmodel)
    {
        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            if(customer.getDevicebindingenabled()){

//                wsmodel.getDevices();
                String dbQuery;
                Map<String, Object> params;
                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DVC ";
                params = new HashMap<String, Object>();
//                logger.info("from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DVC ");
//                logger.info("Zaid:"+wsmodel.getSecurityparams().getDeviceid());
                params.put("DVC", wsmodel.getDevices().get(0).getDeviceid());

                List<MWCustDeviceBindingLog> dbbindlog = GeneralDao.Instance.find(dbQuery, params);

                if(dbbindlog != null && dbbindlog.size() > 0){

                    logger.error("Duplicate Device Binding found, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                    return wsmodel;

//                }
//                if (dbbindlog != null && !dbbindlog.isEmpty())
//                {
//                    // Check if the device is already available in the list
//                    boolean deviceAlreadyExists = false;
//
//                    for (MWCustDeviceBindingLog bindingLog : dbbindlog) {
//                        if (bindingLog.getDevice().equals(wsmodel.getSecurityparams().getDeviceid())) {
//                            deviceAlreadyExists = true;
//                            break; // Found a matching device, no need to continue checking
//                        }
//                    }
//
//                    if (deviceAlreadyExists) {
//                        logger.info("Device Found...");
//                        logger.error("Duplicate Device Binding found, rejecting...");
//                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
//                    }
//                    logger.info("Device not null..");
//                    return wsmodel;
                }
                else{
                    logger.info("No Device Binding Log record found for customer [" + customer.getMobilenumber() + "] device [" + wsmodel.getDevices().get(0).getDeviceid() + "], logging...");

                    dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                    params = new HashMap<String, Object>();
                    params.put("DEVC", wsmodel.getDevices().get(0).getDeviceid());

                    MWDeviceLog device = (MWDeviceLog)GeneralDao.Instance.findObject(dbQuery, params);


                    if(device == null)
                    {
                        device = new MWDeviceLog();
                        device.setDeviceid(wsmodel.getDevices().get(0).getDeviceid());
                        device.setFirebasetoken(wsmodel.getDevices().get(0).getFirebasetoken());
                        device.setDevicemodel(wsmodel.getDevices().get(0).getDevicemodel());
                        device.setOperatingsystem(wsmodel.getDevices().get(0).getOperatingsystem());
                        device.setScreenresolution(wsmodel.getDevices().get(0).getScreenresolution());
                        device.setBaseintegrity(false);
                        device.setCtsprofile(false);
                        device.setIsrooted(false);
                        device.setImei(wsmodel.getDevices().get(0).getImei());
                        device.setImsi(wsmodel.getDevices().get(0).getImsi());
                        device.setIccid(wsmodel.getDevices().get(0).getIccid());
                        device.setUuid(wsmodel.getDevices().get(0).getUuid());

                        device.setCustomer(customer);
                        GeneralDao.Instance.save(device);
                    }

                    MWCustDeviceBindingLog dlog = new MWCustDeviceBindingLog();
                    dlog.setDevice(device);
                    dlog.setCustomer(customer);
                    dlog.setStatus(MWCustDevcBindStatus.ACTIVE);
                    dlog.setCreatedate(new Date());
                    dlog.setLastupdatedate(new Date());
                    dlog.setReason(MWCustDevcBindReasons.MANUAL);
                    GeneralDao.Instance.save(dlog);

                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    return wsmodel;
                }
            }
            else{
                logger.error("Customer Device Binding disabled, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return wsmodel;
            }
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

    public static AppWsEntity processRemoveDeviceFromBindingRequest(AppWsEntity wsmodel)
    {
        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

        MWCustomer customer = GetCustomer(wsmodel.getMobilenumber());

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE)) {

            if(customer.getDevicebindingenabled()){

                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DVC and c.customer= :CUST ";
                params = new HashMap<String, Object>();
//                params.put("DVC", wsmodel.getSecurityparams().getDeviceid());
                params.put("DVC", wsmodel.getDevices().get(0).getDeviceid());
                params.put("CUST", customer);

                MWCustDeviceBindingLog dbbindlog = (MWCustDeviceBindingLog)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbbindlog != null){

                    String deletequery = "";
                    String countQuery = "";

                    try {
                        deletequery = "delete MWCustDeviceBindingLog where id = :ID ";

                        Query query = GeneralDao.Instance.getCurrentSession().createQuery(deletequery);
                        query.setParameter("ID", dbbindlog.getId());

                        int rowCount = query.executeUpdate();
                        if (rowCount == 0) {
                            logger.info("MWCustDeviceBindingLog No data found to delete");
                        } else {
                            logger.info("MWCustDeviceBindingLog is deleted");

                            countQuery = "from " + MWCustomerDeviceUnbindingCount.class.getName() + " c where c.customer= :CUST ";

                            params = new HashMap<String, Object>();
                            params.put("CUST", customer.getId().toString());

                            MWCustomerDeviceUnbindingCount dbcount = (MWCustomerDeviceUnbindingCount)GeneralDao.Instance.findObject(countQuery, params);

                            if (dbcount == null) {
                                dbcount = new MWCustomerDeviceUnbindingCount();
                                dbcount.setCustomerID(customer.getId().toString());
                                dbcount.setMonthlyUnbindingCount(1L);
                                dbcount.setDailyUnbindingCount(1L);
                                GeneralDao.Instance.save(dbcount);
                            } else {
                                dbcount.setMonthlyUnbindingCount(dbcount.getMonthlyUnbindingCount() + 1L);
                                dbcount.setDailyUnbindingCount(dbcount.getDailyUnbindingCount() + 1L);
                                GeneralDao.Instance.saveOrUpdate(dbcount);
                            }

                        }
                    } catch (Exception e) {
                        logger.error("Exception caught while deleting MWCustDeviceBindingLog for ID [" + dbbindlog.getId() + "], ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }


                    //GeneralDao.Instance.delete(dbbindlog);
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    return wsmodel;
                }
                else{
                    logger.error("No Device Binding Log record found for customer [" + customer.getMobilenumber() + "] device [" + wsmodel.getDevices().get(0).getDeviceid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                    return wsmodel;
                }
            }
            else{
                logger.error("Customer Device Binding disabled, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return wsmodel;
            }
        }
        else{
            logger.error("Inactive or no customer found against Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }
    }

    //Device Unbinding - Nofel start

//    public static AppWsEntity processConfirmRemoveDeviceFromBindingRequest(AppWsEntity wsmodel) {
//        if (wsmodel == null) {
//            logger.error("Received null wsmodel, rejecting...");
//            return createErrorResponse(null, ISOResponseCodes.UNABLE_TO_PROCESS, "wsmodel is null");
//        }
//
//        logger.info("Validating Session...");
//
//        try {
//            if (!MWWSOperation.ValidateUserandAppSession(wsmodel)) {
//                logger.error("Failed to validate session, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
//                return wsmodel;
//            }
//        } catch (Exception e) {
//            logger.error("Exception caught during session validation: {}", WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
//            return wsmodel;
//        }
//
//        MWCustomer customer = null;
//        try {
//            customer = GetCustomer(wsmodel.getMobilenumber());
//        } catch (Exception e) {
//            logger.error("Error fetching customer for mobile [{}]: {}", wsmodel.getMobilenumber(), WebServiceUtil.getStrException(e));
//        }
//
//        if (customer == null) {
//            logger.error("No customer found for mobile [{}], rejecting...", wsmodel.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
//            return wsmodel;
//        }
//
//        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
//            logger.error("Customer [{}] is inactive, rejecting...", customer.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
//            return wsmodel;
//        }
//
//        Boolean deviceBindingEnabled = customer.getDevicebindingenabled();
//        if (deviceBindingEnabled == null || !deviceBindingEnabled) {
//            logger.error("Customer device binding disabled or null, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
//            return wsmodel;
//        }
//
//        // Defensive checks for wsmodel.getDevices()
////        if (wsmodel.getDevices() == null || wsmodel.getDevices().isEmpty() || wsmodel.getDevices().get(0) == null) {
////            logger.error("No devices found in wsmodel, rejecting...");
////            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
////            return wsmodel;
////        }
//
////        String deviceId = wsmodel.getDevices().get(0).getDeviceid(); temporarily changing to fetch deviceID from security params
//        String deviceId = wsmodel.getSecurityparams().getDeviceid();
//        if (deviceId == null || deviceId.trim().isEmpty()) {
//            logger.error("Device ID is missing in wsmodel, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//            return wsmodel;
//        }
//
//        String dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
//        Map<String, Object> params = new HashMap<>();
//        params.put("DVC", deviceId);
//        params.put("CUST", customer);
//
//        MWCustDeviceBindingLog dbbindlog = null;
//
//        try {
//            dbbindlog = (MWCustDeviceBindingLog) GeneralDao.Instance.findObject(dbQuery, params);
//        } catch (Exception e) {
//            logger.error("Error while fetching device binding log for device [{}]: {}", deviceId, WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        if (dbbindlog == null) {
//            logger.error("No Device Binding Log record found for customer [{}], device [{}], rejecting...",
//                    customer.getMobilenumber(), deviceId);
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        // Delete safely
//        try {
//            String deletequery = "delete MWCustDeviceBindingLog where id = :ID";
//            Query query = GeneralDao.Instance.getCurrentSession().createQuery(deletequery);
//            query.setParameter("ID", dbbindlog.getId());
//
//            int rowCount = query.executeUpdate();
//            if (rowCount == 0) {
//                logger.info("MWCustDeviceBindingLog: no data found to delete (ID={}) , (CUSTOMER={}), (DEVICE={})"
//                        , dbbindlog.getId(), dbbindlog.getCustomer().getCustomerId(), dbbindlog.getDevice().getDeviceid());
//            } else {
//                logger.info("MWCustDeviceBindingLog deleted successfully (ID={})", dbbindlog.getId());
//
//                MWDeviceBindLogDeletionLog log = new MWDeviceBindLogDeletionLog();
//                log.setCustomer(customer);
//                log.setDevice(dbbindlog.getDevice());
//                log.setDeletionDateTime(new DateTime());
//                log.setReason(wsmodel.getReason());
//
//                GeneralDao.Instance.saveOrUpdate(log);
//
//                logger.info("Device binding has been disabled and the following deleted entry has been logged to the MW__DEVC_BIND_LOG_DELETION_LOG table");
//
//            }
//        } catch (Exception e) {
//            logger.error("Exception caught while deleting MWCustDeviceBindingLog (ID={}): {}",
//                    dbbindlog != null ? dbbindlog.getId() : "null",
//                    WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//        return wsmodel;
//    }

//    public static AppWsEntity processConfirmRemoveDeviceFromBindingRequest(AppWsEntity wsmodel) {
//        if (wsmodel == null) {
//            logger.error("Received null wsmodel, rejecting...");
//            return createErrorResponse(null, ISOResponseCodes.UNABLE_TO_PROCESS, "wsmodel is null");
//        }
//
//        logger.info("Validating Session...");
//
//        try {
//            if (!MWWSOperation.ValidateUserandAppSession(wsmodel)) {
//                logger.error("Failed to validate session, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
//                return wsmodel;
//            }
//        } catch (Exception e) {
//            logger.error("Exception caught during session validation: {}", WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
//            return wsmodel;
//        }
//
//        // Validate mobile number exists
//        if (wsmodel.getMobilenumber() == null || wsmodel.getMobilenumber().trim().isEmpty()) {
//            logger.error("Mobile number is missing in wsmodel, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        MWCustomer customer = null;
//        try {
//            customer = GetCustomer(wsmodel.getMobilenumber());
//        } catch (Exception e) {
//            logger.error("Error fetching customer for mobile [{}]: {}", wsmodel.getMobilenumber(), WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);  // FIX: Set response code
//            return wsmodel;
//        }
//
//        if (customer == null) {
//            logger.error("No customer found for mobile [{}], rejecting...", wsmodel.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
//            return wsmodel;
//        }
//
//        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
//            logger.error("Customer [{}] is inactive, rejecting...", customer.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
//            return wsmodel;
//        }
//
//        Boolean deviceBindingEnabled = customer.getDevicebindingenabled();
//        if (deviceBindingEnabled == null || !deviceBindingEnabled) {
//            logger.error("Customer [{}] device binding disabled or null, rejecting...", customer.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
//            return wsmodel;
//        }
//
//        // FIX: Check if security params exists before accessing
//        if (wsmodel.getSecurityparams() == null) {
//            logger.error("Security params missing in wsmodel, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//            return wsmodel;
//        }
//
//        String deviceId = wsmodel.getSecurityparams().getDeviceid();
//        if (deviceId == null || deviceId.trim().isEmpty()) {
//            logger.error("Device ID is missing in security params, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//            return wsmodel;
//        }
//
//        String dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
//        Map<String, Object> params = new HashMap<>();
//        params.put("DVC", deviceId);
//        params.put("CUST", customer);
//
//        MWCustDeviceBindingLog dbbindlog = null;
//
//        try {
//            dbbindlog = (MWCustDeviceBindingLog) GeneralDao.Instance.findObject(dbQuery, params);
//        } catch (Exception e) {
//            logger.error("Error while fetching device binding log for device [{}]: {}", deviceId, WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        if (dbbindlog == null) {
//            logger.error("No Device Binding Log record found for customer [{}], device [{}], rejecting...",
//                    customer.getMobilenumber(), deviceId);
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        // FIX: Validate device exists in binding log
//        if (dbbindlog.getDevice() == null) {
//            logger.error("Device reference is null in binding log (ID={}), rejecting...", dbbindlog.getId());
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        // Delete the binding log
//        try {
//            String deletequery = "delete MWCustDeviceBindingLog where id = :ID";
//            Query query = GeneralDao.Instance.getCurrentSession().createQuery(deletequery);
//            query.setParameter("ID", dbbindlog.getId());
//
//            int rowCount = query.executeUpdate();
//            if (rowCount == 0) {
//                // FIX: Null-safe logging
//                logger.warn("MWCustDeviceBindingLog: no data found to delete (ID={}) , (CUSTOMER={}), (DEVICE={})",
//                        dbbindlog.getId(),
//                        dbbindlog.getCustomer() != null ? dbbindlog.getCustomer().getCustomerId() : "null",
//                        dbbindlog.getDevice() != null ? dbbindlog.getDevice().getDeviceid() : "null");
//                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//                return wsmodel;
//            } else {
//                logger.info("MWCustDeviceBindingLog deleted successfully (ID={}), (CUSTOMER={}), (DEVICE={})"
//                        , dbbindlog.getId(), dbbindlog.getCustomer().getCustomerId(), dbbindlog.getDevice().getDeviceid());
//
//                // FIX: Wrap deletion logging in separate try-catch to not fail the main operation
//                try {
//                    MWDeviceBindLogDeletionLog log = new MWDeviceBindLogDeletionLog();
//                    log.setCustomer(customer);
//                    log.setDevice(dbbindlog.getDevice());
//                    log.setDeletionDateTime(new DateTime());
//
//                    // FIX: Handle null reason
//                    String reason = wsmodel.getReason();
//                    log.setReason(reason != null && !reason.trim().isEmpty() ? reason : "No reason provided");
//
//                    GeneralDao.Instance.saveOrUpdate(log);
//
//                    logger.info("Device binding deletion logged successfully to MW__DEVC_BIND_LOG_DELETION_LOG (Customer={}, Device={})",
//                            customer.getCustomerId(), dbbindlog.getDevice().getDeviceid());
//
//                    //Next we update the mw_cust_devcs_sess_log entry
//                    logger.info("Proceeding to update the MW_CUST_DEVCS_SESS_LOG entry/entries for device [{}] and customer [{}]"
//                            , dbbindlog.getDevice().getDeviceid(), dbbindlog.getCustomer().getCustomerId());
//
//                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
//                    params = new HashMap<>();
//                    params.put("DVC", dbbindlog.getDevice().getDeviceid());
//                    params.put("CUST", customer);
//
//                    List<MWDeviceSessionLog> dbsesslog = null;
//                    dbsesslog = GeneralDao.Instance.find(dbQuery, params);
//
//                    //just set the customer to null for all
//                    if (dbsesslog != null && !dbsesslog.isEmpty()) {
//                        for (MWDeviceSessionLog sess : dbsesslog) {
//                            sess.setCustomer(null);
//                            GeneralDao.Instance.saveOrUpdate(sess);
//                        }
//                        logger.info("Updated {} MWDeviceSessionLog entries to dissociate customer [{}] from device [{}]",
//                                dbsesslog.size(), customer.getCustomerId(), dbbindlog.getDevice().getDeviceid());
//                    } else {
//                        logger.warn("No MWDeviceSessionLog entries found for customer [{}] and device [{}]",
//                                customer.getCustomerId(), dbbindlog.getDevice().getDeviceid());
//                        wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
//                    }
//                } catch (Exception logException) {
//                    // Log the error but don't fail the operation since binding was deleted successfully
//                    logger.error("Failed to log deletion to MW__DEVC_BIND_LOG_DELETION_LOG: {}",
//                            WebServiceUtil.getStrException(logException));
//                    // Continue - the main deletion operation succeeded
//                }
//
//            }
//        } catch (Exception e) {
//            logger.error("Exception caught while deleting MWCustDeviceBindingLog (ID={}): {}",
//                    dbbindlog.getId(), WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//        return wsmodel;
//    }
//
//
//    private static AppWsEntity createErrorResponse(AppWsEntity wsmodel, String respCode, String message) {
//        AppWsEntity response = (wsmodel != null) ? wsmodel : new AppWsEntity();
//        response.setRespcode(respCode);
//        logger.error(message);
//        return response;
//    }


//    public static AppWsEntity processConfirmRemoveDeviceFromBindingRequest(AppWsEntity wsmodel) {
//        if (wsmodel == null) {
//            logger.error("Received null wsmodel, rejecting...");
//            return wsmodel;
//        }
//
//        logger.info("Validating Session...");
//
//        try {
//            if (!MWWSOperation.ValidateUserandAppSession(wsmodel)) {
//                logger.error("Failed to validate session, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
//                return wsmodel;
//            }
//        } catch (Exception e) {
//            logger.error("Exception caught during session validation: {}", WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
//            return wsmodel;
//        }
//
//        // Validate mobile number exists
//        if (wsmodel.getMobilenumber() == null || wsmodel.getMobilenumber().trim().isEmpty()) {
//            logger.error("Mobile number is missing in wsmodel, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        MWCustomer customer = null;
//        try {
//            customer = GetCustomer(wsmodel.getMobilenumber());
//        } catch (Exception e) {
//            logger.error("Error fetching customer for mobile [{}]: {}", wsmodel.getMobilenumber(), WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        if (customer == null) {
//            logger.error("No customer found for mobile [{}], rejecting...", wsmodel.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
//            return wsmodel;
//        }
//
//        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
//            logger.error("Customer [{}] is inactive, rejecting...", customer.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
//            return wsmodel;
//        }
//
//        Boolean deviceBindingEnabled = customer.getDevicebindingenabled();
//        if (deviceBindingEnabled == null || !deviceBindingEnabled) {
//            logger.error("Customer [{}] device binding disabled or null, rejecting...", customer.getMobilenumber());
//            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
//            return wsmodel;
//        }
//
//        // Check if security params exists before accessing
//        if (wsmodel.getSecurityparams() == null) {
//            logger.error("Security params missing in wsmodel, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//            return wsmodel;
//        }
//
//        String deviceId = wsmodel.getSecurityparams().getDeviceid();
//        if (deviceId == null || deviceId.trim().isEmpty()) {
//            logger.error("Device ID is missing in security params, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//            return wsmodel;
//        }
//
//        String dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
//        Map<String, Object> params = new HashMap<>();
//        params.put("DVC", deviceId);
//        params.put("CUST", customer);
//
//        MWCustDeviceBindingLog dbbindlog = null;
//
//        try {
//            logger.info("Executing the following query to fetch MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG): {}", dbQuery);
//
//            dbbindlog = (MWCustDeviceBindingLog) GeneralDao.Instance.findObject(dbQuery, params);
//        } catch (Exception e) {
//            logger.error("Error while fetching from MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) for device [{}]: {}", deviceId, WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        if (dbbindlog == null) {
//            logger.error("No MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) record found for customer [{}], device [{}], rejecting...",
//                    customer.getMobilenumber(), deviceId);
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        // Validate device exists in binding log
//        if (dbbindlog.getDevice() == null) {
//            logger.error("Device reference is null in MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) (ID={}), rejecting...", dbbindlog.getId());
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        // FIX: Store device info before deletion for later use
//        String deletedDeviceId = dbbindlog.getDevice().getDeviceid();
//        Long deletedBindingId = dbbindlog.getId();
//        MWDeviceLog deviceLog = dbbindlog.getDevice();
//
//        // Delete the binding log
//        try {
//            String deleteQuery = "delete MWCustDeviceBindingLog where id = :ID";
//            Query query = GeneralDao.Instance.getCurrentSession().createQuery(deleteQuery);
//            query.setParameter("ID", deletedBindingId);
//
//            logger.info("Executing the following delete query on MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG): {}", deleteQuery);
//
//            int rowCount = query.executeUpdate();
//            if (rowCount == 0) {
//                logger.warn("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG): no data found to delete (ID={}) , (CUSTOMER={}), (DEVICE={})",
//                        deletedBindingId,
//                        dbbindlog.getCustomer() != null ? dbbindlog.getCustomer().getCustomerId() : "null",
//                        deletedDeviceId);
//                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//                return wsmodel;
//            }
//
//            logger.info("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) deleted successfully (ID={}), (CUSTOMER={}), (DEVICE={})",
//                    deletedBindingId, customer.getCustomerId(), deletedDeviceId);
//
//            // FIX: Track success of post-deletion operations
//            boolean deletionLogSuccess = false;
//            boolean sessionLogUpdateSuccess = false;
//
//            // Log the deletion
//            try {
//                MWDeviceBindLogDeletionLog log = new MWDeviceBindLogDeletionLog();
//                log.setCustomer(customer);
//                log.setDevice(deviceLog);
//                log.setDeletionDateTime(new Date());
//
//                String reason = wsmodel.getReason();
//                log.setReason(reason != null && !reason.trim().isEmpty() ? reason : "No reason provided");
//
//                logger.info("Logging deletion to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG) for customer [{}], device [{}]",
//                        customer.getCustomerId(), deletedDeviceId);
//
//                GeneralDao.Instance.saveOrUpdate(log);
//                deletionLogSuccess = true;
//
//                logger.info("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) deletion logged successfully to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG) (Customer={}, Device={})",
//                        customer.getCustomerId(), deletedDeviceId);
//
//            } catch (Exception logException) {
//                logger.error("Failed to log deletion to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG): {}",
//                        WebServiceUtil.getStrException(logException));
//                // Continue - the main deletion operation succeeded
//            }
//
//            // Update session log entries
//            try {
//                logger.info("Proceeding to update the MWDeviceLog(MW_CUST_DEVC_LOG) entry/entries for device [{}] and customer [{}]",
//                        deletedDeviceId, customer.getCustomerId());
//
//                String sessDbQuery = "from " + MWDeviceLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
//                Map<String, Object> sessParams = new HashMap<>();
//                sessParams.put("DVC", deletedDeviceId);
//                sessParams.put("CUST", customer);
//
//                logger.info("Executing the following query to fetch MWDeviceLog(MW_CUST_DEVC_LOG): {}", sessDbQuery);
//
//                List<MWDeviceSessionLog> dbsesslog = GeneralDao.Instance.find(sessDbQuery, sessParams);
//
//                if (dbsesslog != null && !dbsesslog.isEmpty()) {
//                    for (MWDeviceSessionLog sess : dbsesslog) {
//                        sess.setCustomer(null);
//
//                        logger.info("Dissociating customer [{}] from MWDeviceSessionLog(MW_CUST_DEVC_LOG) entry (ID={})",
//                                customer.getCustomerId(), sess.getId());
//
//                        GeneralDao.Instance.saveOrUpdate(sess);
//                    }
//                    sessionLogUpdateSuccess = true;
//                    logger.info("Updated (all) {} MWDeviceSessionLog(MW_CUST_DEVC_LOG) entries to dissociate customer [{}] from device [{}]",
//                            dbsesslog.size(), customer.getCustomerId(), deletedDeviceId);
//                } else {
//                    logger.warn("No MWDeviceSessionLog(MW_CUST_DEVC_LOG) entries found for customer [{}] and device [{}]",
//                            customer.getCustomerId(), deletedDeviceId);
//                    // FIX: This is not necessarily an error - device might not have session logs yet
//                    sessionLogUpdateSuccess = true; // Consider this success
//                }
//
//            } catch (Exception sessException) {
//                logger.error("Failed to update MWDeviceSessionLog(MW_CUST_DEVC_LOG) entries: {}",
//                        WebServiceUtil.getStrException(sessException));
//                // Continue - the main deletion operation succeeded
//            }
//
//            // FIX: Set response code based on overall success
//            // Main operation (deletion) succeeded, so return APPROVED
//            // Post-deletion operations are logged but don't affect success status
//            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//
//            // FIX: Optionally add warnings to response if post-operations failed
//            if (!deletionLogSuccess || !sessionLogUpdateSuccess) {
//                logger.warn("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) removed successfully, but some post-deletion operations had issues. " +
//                        "DeletionLog={}, SessionLogUpdate={}", deletionLogSuccess, sessionLogUpdateSuccess);
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception caught while deleting MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) (ID={}): {}",
//                    deletedBindingId, WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//
//        return wsmodel;
//    }


    public static AppWsEntity processConfirmRemoveDeviceFromBindingRequest(AppWsEntity wsmodel) {
        if (wsmodel == null) {
            logger.error("Received null wsmodel, rejecting...");
            wsmodel = new AppWsEntity();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        logger.info("Validating Session...");

        try {
            if (!MWWSOperation.ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to validate session, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error("Exception caught during session validation: {}", WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
            return wsmodel;
        }

        // Validate mobile number exists
        if (wsmodel.getMobilenumber() == null || wsmodel.getMobilenumber().trim().isEmpty()) {
            logger.error("Mobile number is missing in wsmodel, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_MOBILENUM);
            return wsmodel;
        }

        MWCustomer customer = null;
        try {
            customer = GetCustomer(wsmodel.getMobilenumber());
        } catch (Exception e) {
            logger.error("Error fetching customer for mobile [{}]: {}", wsmodel.getMobilenumber(), WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
            return wsmodel;
        }

        if (customer == null) {
            logger.error("No customer found for mobile [{}], rejecting...", wsmodel.getMobilenumber());
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
            return wsmodel;
        }

        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
            logger.error("Customer [{}] is inactive, rejecting...", customer.getMobilenumber());
            wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            return wsmodel;
        }

        Boolean deviceBindingEnabled = customer.getDevicebindingenabled();
        if (deviceBindingEnabled == null || !deviceBindingEnabled) {
            logger.error("Customer [{}] device binding disabled or null, rejecting...", customer.getMobilenumber());
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }

        // Check if security params exists before accessing
        if (wsmodel.getSecurityparams() == null) {
            logger.error("Security params missing in wsmodel, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);
            return wsmodel;
        }

        String deviceId = wsmodel.getSecurityparams().getDeviceid();
        if (deviceId == null || deviceId.trim().isEmpty()) {
            logger.error("Device ID is missing in security params, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.DEVICE_MISSING);
            return wsmodel;
        }

        // FIX: Query for ALL matching entries (handle duplicates)
        String dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid = :DVC and c.customer = :CUST";
        Map<String, Object> params = new HashMap<>();
        params.put("DVC", deviceId);
        params.put("CUST", customer);

        List<MWCustDeviceBindingLog> dbbindlogs = null;

        try {
            logger.info("Executing the following query to fetch MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG): {}", dbQuery);
            logger.info("Query parameters: DEVICE=[{}], CUSTOMER=[{}]", deviceId, customer.getId());

            dbbindlogs = GeneralDao.Instance.find(dbQuery, params);

            if (dbbindlogs != null && !dbbindlogs.isEmpty()) {
                logger.info("Found {} MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) record(s) for customer [{}], device [{}]",
                        dbbindlogs.size(), customer.getMobilenumber(), deviceId);

                // FIX: Warn if duplicates found
                if (dbbindlogs.size() > 1) {
                    logger.warn("WARNING: Found {} duplicate MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) entries for customer [{}], device [{}]. Will delete all.",
                            dbbindlogs.size(), customer.getId(), deviceId);

                    // Log all duplicate IDs for tracking
                    StringBuilder duplicateIds = new StringBuilder("Duplicate entry IDs: ");
                    for (MWCustDeviceBindingLog log : dbbindlogs) {
                        duplicateIds.append(log.getId()).append(", ");
                    }
                    logger.warn(duplicateIds.toString());
                }
            }
        } catch (Exception e) {
            logger.error("Error while fetching from MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) for device [{}]: {}",
                    deviceId, WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        if (dbbindlogs == null || dbbindlogs.isEmpty()) {
            logger.error("No MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) record found for customer [{}], device [{}], rejecting...",
                    customer.getMobilenumber(), deviceId);
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        // FIX: Validate all entries have valid device references
        for (MWCustDeviceBindingLog bindlog : dbbindlogs) {
            if (bindlog.getDevice() == null) {
                logger.error("Device reference is null in MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) (ID={}), rejecting...",
                        bindlog.getId());
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
        }

        // FIX: Store device info from first valid entry (all should have same device)
        String deletedDeviceId = dbbindlogs.get(0).getDevice().getDeviceid();
        MWDeviceLog deviceLog = dbbindlogs.get(0).getDevice();

        // FIX: Delete ALL binding log entries
        try {
            int totalDeleted = 0;
            List<Long> deletedIds = new ArrayList<>();

            // FIX: Delete each entry individually to ensure proper logging
            for (MWCustDeviceBindingLog dbbindlog : dbbindlogs) {
                Long deletedBindingId = dbbindlog.getId();

                String deleteQuery = "delete MWCustDeviceBindingLog where id = :ID";
                Query query = GeneralDao.Instance.getCurrentSession().createQuery(deleteQuery);
                query.setParameter("ID", deletedBindingId);

                logger.info("Executing delete query on MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) for ID=[{}]", deletedBindingId);

                int rowCount = query.executeUpdate();
                if (rowCount == 0) {
                    logger.warn("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG): no data found to delete (ID={}) , (CUSTOMER={}), (DEVICE={})",
                            deletedBindingId,
                            dbbindlog.getCustomer() != null ? dbbindlog.getCustomer().getCustomerId() : "null",
                            deletedDeviceId);
                } else {
                    totalDeleted += rowCount;
                    deletedIds.add(deletedBindingId);
                    logger.info("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) entry deleted successfully (ID={}), (CUSTOMER={}), (DEVICE={})",
                            deletedBindingId, customer.getCustomerId(), deletedDeviceId);
                }
            }

            if (totalDeleted == 0) {
                logger.error("Failed to delete any MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) entries");
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }

            logger.info("Successfully deleted {} MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) entry/entries (CUSTOMER={}), (DEVICE={}). Deleted IDs: {}",
                    totalDeleted, customer.getCustomerId(), deletedDeviceId, deletedIds);

            // FIX: Track success of post-deletion operations
            boolean deletionLogSuccess = false;
            boolean sessionLogUpdateSuccess = false;

            // FIX: Log the deletion for each deleted entry
            try {
                for (Long deletedId : deletedIds) {
                    MWDeviceBindLogDeletionLog log = new MWDeviceBindLogDeletionLog();
                    log.setCustomer(customer);
                    log.setDevice(deviceLog);
                    log.setDeletionDateTime(new Date());

                    String reason = wsmodel.getReason();
                    log.setReason(reason != null && !reason.trim().isEmpty() ? reason : "No reason provided");

                    logger.info("Logging deletion to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG) for deleted entry ID=[{}], customer=[{}], device=[{}]",
                            deletedId, customer.getCustomerId(), deletedDeviceId);

                    GeneralDao.Instance.saveOrUpdate(log);
                }

                deletionLogSuccess = true;
                logger.info("All {} MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) deletions logged successfully to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG) (Customer={}, Device={})",
                        deletedIds.size(), customer.getCustomerId(), deletedDeviceId);

            } catch (Exception logException) {
                logger.error("Failed to log deletion to MWDeviceBindLogDeletionLog(MW__DEVC_BIND_LOG_DELETION_LOG): {}",
                        WebServiceUtil.getStrException(logException));
                // Continue - the main deletion operation succeeded
            }

            // Update device customer log entries
            try {
                logger.info("Proceeding to update the MWDeviceLog(MW_CUST_DEVC_LOG) entry/entries for device [{}] and customer [{}]",
                        deletedDeviceId, customer.getCustomerId());

                String deviceDbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid = :DVC and c.customer = :CUST";
                Map<String, Object> deviceParams = new HashMap<>();
                deviceParams.put("DVC", deletedDeviceId);
                deviceParams.put("CUST", customer);

                logger.info("Executing the following query to fetch MWDeviceLog(MW_CUST_DEVC_LOG): {}", deviceDbQuery);

                List<MWDeviceLog> mwDeviceLogs = GeneralDao.Instance.find(deviceDbQuery, deviceParams);

                if (mwDeviceLogs != null && !mwDeviceLogs.isEmpty()) {
                    logger.info("Found {} MWDeviceLog(MW_CUST_DEVC_LOG) entry/entries to update", mwDeviceLogs.size());

                    for (MWDeviceLog mwDeviceLog : mwDeviceLogs) {
                        mwDeviceLog.setCustomer(null);

                        logger.info("Dissociating customer [{}] from MWDeviceLog(MW_CUST_DEVC_LOG) entry (ID={})",
                                customer.getCustomerId(), mwDeviceLog.getId());

                        GeneralDao.Instance.saveOrUpdate(mwDeviceLog);
                    }
                    sessionLogUpdateSuccess = true;
                    logger.info("Updated (all) {} MWDeviceLog(MW_CUST_DEVC_LOG) entries to dissociate customer [{}] from device [{}]",
                            mwDeviceLogs.size(), customer.getCustomerId(), deletedDeviceId);
                } else {
                    logger.warn("No MWDeviceLog(MW_CUST_DEVC_LOG) entries found for customer [{}] and device [{}]",
                            customer.getCustomerId(), deletedDeviceId);
                    // This is not necessarily an error - device might not have session logs yet
                    sessionLogUpdateSuccess = true; // Consider this success
                }

            } catch (Exception sessException) {
                logger.error("Failed to update MWDeviceLog(MW_CUST_DEVC_LOG) entries: {}",
                        WebServiceUtil.getStrException(sessException));
                // Continue - the main deletion operation succeeded
            }

            //check if the remaining count of distinct entries in the mw_cust_devc_bind_log for this customer is how much if 0 we will disable devicebinding flag for the customer

            try {
                //
                String countQuery = "select count(distinct c.device.deviceid) from " + MWCustDeviceBindingLog.class.getName() + " c where c.customer = :CUST";
                Map<String, Object> countParams = new HashMap<>();
                countParams.put("CUST", customer);

                Long remainingCount = (Long) GeneralDao.Instance.findObject(countQuery, countParams);
                logger.info("Customer [{}] has {} remaining distinct device bindings after deletion", customer.getCustomerId(), remainingCount);


                if (remainingCount - 1 <= 0) {
                    customer.setDevicebindingenabled(false);
                    GeneralDao.Instance.saveOrUpdate(customer);
                    logger.info("No remaining device bindings for customer [{}]. Disabled device binding flag.", customer.getCustomerId());
                }
            } catch (Exception ex) {
                logger.error("Failed to check/update device binding flag for customer [{}]: {}",
                        customer.getCustomerId(), WebServiceUtil.getStrException(ex));
                // Continue - the main deletion operation succeeded
            }

            // Set response code based on overall success
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            // Optionally add warnings to response if post-operations failed
            if (!deletionLogSuccess || !sessionLogUpdateSuccess) {
                logger.warn("MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) removed successfully, but some post-deletion operations had issues. " +
                        "DeletionLog={}, DeviceLogUpdate={}", deletionLogSuccess, sessionLogUpdateSuccess);
            }

        } catch (Exception e) {
            logger.error("Exception caught while deleting MWCustDeviceBindingLog(MW_CUST_DEVC_BIND_LOG) entries: {}",
                    WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

        return wsmodel;
    }


    public static MWCustomer GetCustomer(String mobilenum)
    {
        String dbQuery;
        Map<String, Object> params;


        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
        params = new HashMap<String, Object>();
        params.put("MOBNO", mobilenum);

        return (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
    }

    public static Boolean BindCustomerDevice(MWCustomer customer, MWDeviceLog device, String category){

        try{
            String dbQuery;
            Map<String, Object> params;
            Long maxalloweddevices = 5L;
            try{
                dbQuery = "from " + MWCustDeviceBindConfig.class.getName() + " c where c.customertype= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", CustomerType.CUSTOMER);

                MWCustDeviceBindConfig dbconfig = (MWCustDeviceBindConfig)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbconfig != null && dbconfig.getMaxalloweddevices() > 0L){
                    maxalloweddevices =  dbconfig.getMaxalloweddevices();
                }
            }
            catch (Exception e){
                logger.error("Exception caught while getting MWCustDeviceBindConfig, ignoring...");
                logger.error(WebServiceUtil.getStrException(e));
            }


            if(!Util.hasText(category) || (Util.hasText(category) && !category.equals(MWCustDevcBindReasons.SIGNUP))){
                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.customer= :CUST and c.status = :STATUS ";
                params = new HashMap<String, Object>();
                params.put("CUST", customer);
                params.put("STATUS", "00");

                List<MWCustDeviceBindingLog> dbbindlog = GeneralDao.Instance.find(dbQuery, params);

                if(dbbindlog != null && dbbindlog.size() >= maxalloweddevices){
                    logger.error("Max allowed Devices Binded to Customer [" + customer.getMobilenumber() + "], rejecting...");
                    return false;
                }
            }

            //Check whether device is already binded to another customer
            dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DEVC ";
            params = new HashMap<String, Object>();
            params.put("DEVC", device.getDeviceid());

            MWCustDeviceBindingLog dbbindlog = (MWCustDeviceBindingLog)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbbindlog != null && (dbbindlog.getCustomer().getMobilenumber() != customer.getMobilenumber())
            && ((!Util.hasText(category)) ||  Util.hasText(category) && !category.equals(MWCustDevcBindReasons.SIGNUP))){
                logger.info("Duplicate Device Bind record found in DB for DeviceId [" + device.getDeviceid() + "] with Customer [" + customer.getMobilenumber() + "], not binding device...");
                return false;
            }
            else if(dbbindlog != null && Util.hasText(category) && category.equals(MWCustDevcBindReasons.SIGNUP)){
                logger.info("Duplicate Device Bind record found in DB for DeviceId [" + device.getDeviceid() + "] with Customer [" + customer.getMobilenumber() + "], for SignUp, not binding device...");
                return false;
            } else {
                MWCustDeviceBindingLog dblog = new MWCustDeviceBindingLog();
                dblog.setCustomer(customer);
                dblog.setReason(category);
                dblog.setDevice(device);
                dblog.setCreatedate(new Date());
                dblog.setLastupdatedate(new Date());
                dblog.setStatus(MWCustDevcBindStatus.ACTIVE);
                GeneralDao.Instance.save(dblog);
            }

            return true;
        }
        catch (Exception e){
            logger.error("Exception caught while binding device [" + device.getDeviceid() + "] with Customer [" + customer.getMobilenumber() + "] for [" + category + "]");
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }
}
