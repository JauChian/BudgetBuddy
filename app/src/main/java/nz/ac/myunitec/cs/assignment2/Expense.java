package nz.ac.myunitec.cs.assignment2;

import java.util.Date;
import java.util.Locale;

public class Expense {
    private String name;
    private double amount;
    private String category;
    private Date date;
    private String documentId;

    public Expense() {

    }
    public Expense(String name, double amount, String category, Date date, String documentId) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
