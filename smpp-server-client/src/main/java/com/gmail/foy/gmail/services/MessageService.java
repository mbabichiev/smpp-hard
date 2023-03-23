package com.gmail.foy.gmail.services;

import java.io.IOException;
import java.util.Date;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String SYSID = "j";
    private static final String SYSTYPE = "cp";
    private static final String PASSWORD = "jpwd";
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();


    public int sendMessageToServer(String message) {

        SMPPSession session = new SMPPSession();
        // Set listener to receive deliver_sm
        session.setMessageReceiverListener(new MessageReceiverListener() {

            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
                if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
                    // delivery receipt
                    try {
                        DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                        long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                        String messageId = Long.toString(id, 16).toUpperCase();
                        log.info("received '{}' : {}", messageId, delReceipt);
                    } catch (InvalidDeliveryReceiptException e) {
                        log.error("receive failed, e");
                    }
                } else {
                    // regular short message
                    log.info("Receiving message : {}", new String(deliverSm.getShortMessage()));
                }
            }

            public void onAcceptAlertNotification(AlertNotification alertNotification) {
                log.info("Receiving alert for {} from {}", alertNotification.getSourceAddr(), alertNotification.getEsmeAddr());
            }

            public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
                log.info("dataSm");
                return null;
            }
        });

        try {

            String systemId = session.connectAndBind(HOST, PORT, new BindParameter(BindType.BIND_TRX, SYSID, PASSWORD, SYSTYPE,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));
            log.info("Connected with SMSC with system id {}", systemId);

            int status = -1;
            // send Message
            try {
                // set RegisteredDelivery
                final RegisteredDelivery registeredDelivery = new RegisteredDelivery();
                registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);

                SubmitSmResult submitSmResult = session.submitShortMessage("CMT", TypeOfNumber.INTERNATIONAL,
                        NumberingPlanIndicator.UNKNOWN, "831", TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN,
                        "328176504657", new ESMClass(), (byte)0, (byte)1, TIME_FORMATTER.format(new Date()), null,
                        registeredDelivery, (byte)0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1,
                                false), (byte)0, message.getBytes());

                String messageId = submitSmResult.getMessageId();
                log.info("Message submitted, message_id is {}", messageId);
                status = 0;
            } catch (PDUException e) {
                // Invalid PDU parameter
                log.error("Invalid PDU parameter", e);
            } catch (ResponseTimeoutException e) {
                // Response timeout
                log.error("Response timeout", e);
            } catch (InvalidResponseException e) {
                // Invalid response
                log.error("Receive invalid response", e);
            } catch (NegativeResponseException e) {
                // Receiving negative response (non-zero command_status)
                log.error("Receive negative response", e);
            } catch (IOException e) {
                log.error("I/O error occurred", e);
            }

            // wait 3 second
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.info("Interrupted exception", e);
            }

            // unbind(disconnect)
            session.unbindAndClose();

            return status;

        } catch (IOException e) {
            log.error("Failed connect and bind to host", e);

            return -1;
        }
    }

}
