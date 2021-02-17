package com.quinos.service;

import com.quinos.dao.Dao;
import com.quinos.model.master.Item;
import com.quinos.model.master.PaymentMethod;
import com.quinos.model.master.Setting;
import com.quinos.model.transaction.Sales;
import com.quinos.model.transaction.SalesLine;
import com.quinos.service.util.Util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JurnalService {
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  
  private static final String SALT = "slt";
  
  private static final String WEBROOT = "https://api.jurnal.id/core";
  
  private static final String API_SUBMIT_INVOICE = "/api/v1/sales_invoices";
  
  private static final String API_SUBMIT_PAYMENT = "/api/v1/receive_payments";
  
  private static final String API_LIST_PRODUCT = "/api/v1/products";
  
  private static final String URL_INVOICE = "";
  
  private static final String API_VOID_INVOICE = "/api/v1/sales_invoices";
  
  private static final String API_VOID_PAYMENT = "/api/v1/receive_payments";
  
  private String apikey = "bfac6ba6531ac7efbede20567d25dcba";
  
  private String integrationKey = "kF/xGjSH3vM/ZH78XNDeKJAfb0e+ssCsvA1Use8NX8phlrw5QFUPeEQeEx5MVYj/5t0DnQV2LWeb\n5i1UyYtNQlUxJtoq+ir/DpNSn3rr0UI=";
  
  private Map<String, String> header = new HashMap<>();
  
  private String lineTaxName = "PPN";
  
  private String personName = "OUTLET1";
  
  private String depositToName = "Kas";
  
  private String warehouseName = "";
  
  private String serviceChargeName = "Service Charge";
  
  private String roundingName = "Rounding";
  
  private String station = "";
  
  private SettingService settingService;
  
  private ItemService itemService;
  
  private Setting setting;
  
  private Dao dao;
  
  public static void main(String[] args) throws Exception {
    JurnalService jurnalService = new JurnalService();
    jurnalService.exportItem();
  }
  
  public void exportItem() throws Exception {
    BufferedWriter out = new BufferedWriter(new FileWriter("item-jurnal.csv"));
    out.write("Name*,Description,ProductCode,*Unit,SellPrice,DefaultSellAccountCode,DefaultSellTaxName,BuyPrice,DefaultBuyAccountCode,DefaultBuyTaxName,#OpeningBalanceQuantity,#OpeningBalanceAveragePrice,#OpeningBalanceDate,#DefaultInventoryAccountCode,BufferQuantity");
    out.write(System.getProperty("line.separator"));
    List<Item> items = this.itemService.listSalesItem();
    for (Item item : items) {
      if (item.isActive()) {
        out.write(item.getName() + ",");
        out.write(item.getName() + ",");
        out.write(item.getCode() + ",");
        out.write(item.getUom() + ",");
        out.write(item.getPrice1() + ",");
        out.write(",");
        out.write(",");
        out.write("0,");
        out.write(",");
        out.write(",");
        out.write("0,");
        out.write("0,");
        out.write(",");
        out.write(",");
        out.write("0,");
        out.write(System.getProperty("line.separator"));
      } 
    } 
    out.close();
  }
  
  public void build() throws Exception {
    this.setting = this.settingService.getSetting();
    JSONObject jurnal = (JSONObject)this.setting.getCustomObject("jurnal");
    if (jurnal != null) {
      if (jurnal.containsKey("api_key"))
        this.apikey = Util.getJSONString(jurnal, "api_key"); 
      if (jurnal.containsKey("tax_name"))
        this.lineTaxName = Util.getJSONString(jurnal, "tax_name"); 
      if (jurnal.containsKey("person_name"))
        this.personName = Util.getJSONString(jurnal, "person_name"); 
      if (jurnal.containsKey("deposit_name"))
        this.depositToName = Util.getJSONString(jurnal, "deposit_name"); 
      if (jurnal.containsKey("warehouse_name"))
        this.warehouseName = Util.getJSONString(jurnal, "warehouse_name"); 
      if (jurnal.containsKey("integration_key"))
        this.integrationKey = Util.getJSONString(jurnal, "integration_key"); 
      if (jurnal.containsKey("station"))
        this.station = Util.getJSONString(jurnal, "station"); 
      if (this.integrationKey.equalsIgnoreCase(Protector.encrypt(this.apikey + this.personName, "slt"))) {
        this.active = true;
        System.out.println("Jurnal service is active");
        if (Util.isEmpty(this.station) || Util.getComputerName().equalsIgnoreCase(this.station))
          startThread(); 
      } else {
        System.out.println("Jurnal integration key is not valid");
      } 
    } 
  }
  
  public void startThread() {
    System.out.println("Starting background service for Jurnal...");
    (new Thread(new Runnable() {
          public void run() {
            while (true) {
              JurnalService.this.submitPendingTransaction();
              try {
                Thread.sleep(300000L);
              } catch (InterruptedException ex) {
                Logger.getLogger(TadaService.class.getName()).log(Level.SEVERE, (String)null, ex);
              } 
            } 
          }
        })).start();
  }
  
  private void submitPendingTransaction() {
    String query = "select id from tbl_sales WHERE closed is true AND voidCheck is false AND trobex = false ORDER BY ID limit 10";
    List<Object> list = this.dao.createSQLQuery(query, null);
    for (int i = 0; i < list.size(); i++) {
      try {
        BigInteger bigInteger = (BigInteger)list.get(i);
        Sales sales = (Sales)this.dao.get(Sales.class, Long.valueOf(bigInteger.longValue()));
        System.out.println("Submit pending transaction for invoice " + sales.getIdNo());
        submit(sales);
      } catch (Exception ex) {
        ex.printStackTrace();
      } 
    } 
  }
  
  public boolean isActive() {
    return this.active;
  }
  
  private Map<String, String> getHeader() {
    this.header.clear();
    this.header.put("content-type", "application/json");
    this.header.put("apikey", this.apikey);
    return this.header;
  }
  
  public void submitThread(final Sales sales) {
    (new Thread(new Runnable() {
          public void run() {
            try {
              JurnalService.this.submit(sales);
            } catch (Exception ex) {
              ex.printStackTrace();
            } 
          }
        })).start();
  }
  
  private JSONParser parser = new JSONParser();
  
  private String getReferenceId(String result) {
    try {
      JSONObject object = (JSONObject)this.parser.parse(result);
      JSONObject salesInvoice = (JSONObject)object.get("sales_invoice");
      return Util.getJSONString(salesInvoice, "id");
    } catch (Exception ex) {
      ex.printStackTrace();
      return result;
    } 
  }
  
  private String getReceivePaymentId(String result) {
    try {
      JSONObject object = (JSONObject)this.parser.parse(result);
      JSONObject salesInvoice = (JSONObject)object.get("receive_payment");
      return Util.getJSONString(salesInvoice, "id");
    } catch (Exception ex) {
      ex.printStackTrace();
      return result;
    } 
  }
  
  private void submit(Sales sales) throws Exception {
    if (sales.getAppsindoId() == null)
      submitSalesNoPayment(sales); 
    for (SalesLine line : sales.getLines()) {
      if (line.getPaymentMethod() != null && line.getRedemptionId() == null)
        submitPayment(sales, line); 
    } 
    if (isSalesFullySubmitted(sales)) {
      String query = "update tbl_sales set trobex = true where id = " + sales.getId();
      System.out.println(query);
      this.dao.executeSQLQuery(query, null);
    } 
  }
  
  private boolean isSalesFullySubmitted(Sales sales) {
    if (sales.getAppsindoId() == null)
      return false; 
    for (SalesLine line : sales.getLines()) {
      if (line.getPaymentMethod() != null && line.getRedemptionId() == null)
        return false; 
    } 
    return true;
  }
  
  public void submitSalesNoPayment(Sales sales) throws Exception {
    System.out.println("Submit to Jurnal...");
    JSONObject data = convertSales(sales);
    System.out.println(getHeader().toString());
    System.out.println(data.toJSONString());
    String result = ApiService.post("https://api.jurnal.id/core/api/v1/sales_invoices", getHeader(), data.toJSONString(), 201);
    System.out.println(result);
    String id = getReferenceId(result);
    String query = "update tbl_sales set appsindoId = '" + id + "' where id = " + sales.getId();
    System.out.println(query);
    this.dao.executeSQLQuery(query, null);
    sales.setAppsindoId(Long.valueOf(id));
    System.out.println(result);
  }
  
  private void submitPayment(Sales sales, SalesLine salesLine) throws Exception {
    JSONObject data = convertPayment(sales, salesLine);
    String result = ApiService.post("https://api.jurnal.id/core/api/v1/receive_payments", getHeader(), data.toJSONString(), 201);
    System.out.println(result);
    String id = getReceivePaymentId(result);
    String query = "update tbl_sales_lines set redemptionId = " + id + " where id = " + salesLine.getId();
    System.out.println(query);
    this.dao.executeSQLQuery(query, null);
    System.out.println(result);
    salesLine.setRedemptionId(Long.valueOf(id));
  }
  
  public void voidInvoice(Sales sales) throws Exception {
    String url = "https://api.jurnal.id/core/api/v1/sales_invoices/" + sales.getAppsindoId();
    String result = ApiService.delete(url, getHeader());
    System.out.println(result);
  }
  
  public void voidPayment(SalesLine line) throws Exception {
    String url = "https://api.jurnal.id/core/api/v1/receive_payments/" + line.getRedemptionId();
    String result = ApiService.delete(url, getHeader());
    System.out.println(result);
  }
  
  private JSONObject convertPayment(Sales sales, SalesLine salesLine) {
    JSONObject data = new JSONObject();
    JSONObject receivePayment = new JSONObject();
    receivePayment.put("transaction_date", DATE_FORMAT.format(sales.getDate()));
    JSONArray records = new JSONArray();
    JSONObject recordLine = new JSONObject();
    recordLine.put("transaction_no", this.personName + sales.getInvoiceId());
    recordLine.put("amount", Double.valueOf(salesLine.getAmount() - salesLine.getChangeAmount()));
    records.add(recordLine);
    receivePayment.put("records_attributes", records);
    receivePayment.put("custom_id", salesLine.getIdNo());
    receivePayment.put("payment_method_name", salesLine.getDescription());
    PaymentMethod paymentMethod = salesLine.getPaymentMethod();
    receivePayment.put("deposit_to_name", Util.isNotEmpty(paymentMethod.getCode()) ? paymentMethod.getCode() : this.depositToName);
    data.put("receive_payment", receivePayment);
    return data;
  }
  
  private JSONObject convertSales(Sales sales) {
    JSONObject result = new JSONObject();
    JSONObject salesInvoice = new JSONObject();
    salesInvoice.put("transaction_date", DATE_FORMAT.format(sales.getDate()));
    JSONArray lines = convertSalesLines(sales);
    if (sales.getServiceChargeAmount() > 0.0D) {
      JSONObject line = new JSONObject();
      line.put("quantity", Integer.valueOf(1));
      line.put("rate", Double.valueOf(sales.getServiceChargeAmount()));
      line.put("product_name", this.serviceChargeName);
      if (this.setting.isTaxOnServiceCharge())
        line.put("line_tax_name", this.lineTaxName); 
      lines.add(line);
    } 
    if (sales.getRoundingAmount() > 0.0D) {
      JSONObject line = new JSONObject();
      line.put("quantity", Integer.valueOf(1));
      line.put("rate", Double.valueOf(sales.getRoundingAmount()));
      line.put("product_name", this.roundingName);
      lines.add(line);
    } 
    salesInvoice.put("transaction_lines_attributes", lines);
    salesInvoice.put("shipping_date", DATE_FORMAT.format(sales.getDate()));
    salesInvoice.put("shipping_price", Integer.valueOf(0));
    salesInvoice.put("shipping_address", "");
    salesInvoice.put("is_shipped", Boolean.valueOf(true));
    salesInvoice.put("reference_no", "");
    salesInvoice.put("tracking_no", "");
    salesInvoice.put("person_name", this.personName);
    JSONArray tags = new JSONArray();
    tags.add(this.personName);
    salesInvoice.put("tags", tags);
    salesInvoice.put("warehouse_name", this.warehouseName);
    salesInvoice.put("transaction_no", this.personName + sales.getInvoiceId());
    salesInvoice.put("source", "Quinos POS");
    salesInvoice.put("use_tax_inclusive", Boolean.valueOf(false));
    if (this.setting.isTaxBeforeDiscount())
      salesInvoice.put("tax_after_discount", Boolean.valueOf(false)); 
    result.put("sales_invoice", salesInvoice);
    return result;
  }
  
  private JSONArray convertSalesLines(Sales sales) {
    JSONArray lines = new JSONArray();
    for (SalesLine salesLine : sales.getLines()) {
      if (salesLine.getItem() != null) {
        if (salesLine.getQuantityAfterVoid() <= 0.0D)
          continue; 
        JSONObject line = new JSONObject();
        line.put("quantity", Double.valueOf(salesLine.getQuantityAfterVoid()));
        line.put("rate", Double.valueOf(salesLine.getUnitPrice()));
        if (salesLine.getDiscountAmount() > 0.0D) {
          line.put("discount", Double.valueOf(salesLine.getDiscountAmount() / salesLine.getSubtotal() * 1.1));
        } else {
          line.put("discount", Integer.valueOf(0));
        } 
        line.put("product_name", salesLine.getItem().getName());
        if (salesLine.getTax1Amount() > 0.0D || salesLine.getTax2Amount() > 0.0D)
          line.put("line_tax_name", this.lineTaxName); 
        lines.add(line);
      } 
    } 
    return lines;
  }
  
  private void getProduct() throws Exception {
    String url = "https://api.jurnal.id/core/api/v1/products?include_archive=true";
    String result = ApiService.get(url, getHeader());
    System.out.println(result);
  }
  
  public void setSettingService(SettingService settingService) {
    this.settingService = settingService;
  }
  
  public void setDao(Dao dao) {
    this.dao = dao;
  }
  
  public void setItemService(ItemService itemService) {
    this.itemService = itemService;
  }
}
