package com.gmail.foy.maxach;

import java.io.IOException;
import java.util.Date;

import com.gmail.foy.maxach.models.Message;
import com.gmail.foy.maxach.services.MessageService;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SMPPClient {

    private static final Logger log = LoggerFactory.getLogger(SMPPClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8056;
    private static final String SYSID = "j";
    private static final String SYSTYPE = "cp";
    private static final String PASSWORD = "jpwd";
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();
    private static final MessageService messageService = new MessageService();


    public static void main(String[] args) {

        String message = "Message from client";

        SMPPSession session = new SMPPSession();
        session.setTransactionTimer(5000);

        // Set listener to receive deliver_sm
        session.setMessageReceiverListener(new MessageReceiverListener() {

            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
                if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
                    // delivery receipt
                    try {
                        DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                        long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                        String messageId = Long.toString(id, 16);
                        log.info("received '{}' : {}", messageId, delReceipt);

                        if(deliverSm.getShortMessage().length != 0) {
                            messageService.updateStatus(messageId);
                        }

                    } catch (InvalidDeliveryReceiptException e) {
                        log.error("receive failed", e);
                    }
                } else {
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


            // send Message
            try {
                // set RegisteredDelivery
                final RegisteredDelivery registeredDelivery = new RegisteredDelivery();
                registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);

                String serviceType = "CMT";

                TypeOfNumber fromTON = TypeOfNumber.INTERNATIONAL;
                NumberingPlanIndicator fromNPI = NumberingPlanIndicator.UNKNOWN;
                String fromAddr = "1616";

                TypeOfNumber toTON = TypeOfNumber.INTERNATIONAL;
                NumberingPlanIndicator toNPI = NumberingPlanIndicator.UNKNOWN;
                String toAddr = "628176504657";

                Alphabet dcs = Alphabet.ALPHA_DEFAULT;

                SubmitSmResult submitSmResult = session.submitShortMessage(
                        serviceType,
                        fromTON,
                        fromNPI,
                        fromAddr,
                        toTON,
                        toNPI,
                        toAddr,
                        new ESMClass(),
                        (byte) 0,
                        (byte) 1,
                        TIME_FORMATTER.format(new Date()),
                        null,
                        registeredDelivery,
                        (byte) 0,
                        new GeneralDataCoding(dcs, MessageClass.CLASS1, false),
                        (byte) 0,
                        message.getBytes());

                messageService.create(new Message(
                        submitSmResult.getMessageId(),
                        System.currentTimeMillis(),
                        fromAddr,
                        fromTON.name(),
                        fromNPI.name(),
                        toAddr,
                        toTON.name(),
                        toNPI.name(),
                        message,
                        dcs.name(),
                        0
                        ));

                log.info("Message submitted, message_id is {}", submitSmResult.getMessageId());

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
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.info("Interrupted exception", e);
            }

            // unbind(disconnect)
            session.unbindAndClose();

        } catch (IOException e) {
            log.error("Failed connect and bind to host", e);
        }
    }
}