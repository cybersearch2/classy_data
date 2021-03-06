package au.com.cybersearch2.example.v2;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/**
 * Another demonstration object we are creating and persisting to the database.
 */
@Entity(name="Complex")
public class ComplexData 
{
	/** This id is generated by the database and set on the object when it is passed to the create method */
    @Id @GeneratedValue
	int id;

	@Column
	long secs;

	@Column
	boolean odd;

	@Column 
	String quote;
	
	ComplexData() 
	{
		// needed by ormlite
	}

	public ComplexData(long millis, String quote) 
	{
		this.secs = millis / 1000;
		this.odd = ((this.secs % 2) == 1);
		this.quote = quote;
	}

	public void setQuote(String value) 
	{
		quote = value;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("secs=").append(secs);
		sb.append(", ").append("odd=").append(odd);
		sb.append(": \"").append(quote).append("\"");
		return sb.toString();
	}

}
