package ro.pub.acs.traffic.utils;

import java.util.*;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class PhoneUtils {

	public static String fetchOwnPhoneNumber(Activity parentActivity) {
		// Note that this does not always work as expected
		TelephonyManager tMgr = (TelephonyManager) parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = tMgr.getLine1Number().replaceAll(" ", "");

		return mPhoneNumber;
	}

	public static Map<String, String> fetchContacts(Activity parentActivity) {

		Map<String, String> mapContactDetails = new HashMap<String, String>();

		String phoneNumber = null;

		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
		String _ID = ContactsContract.Contacts._ID;
		String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
		String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

		Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
		String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

		ContentResolver contentResolver = parentActivity.getContentResolver();
		Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null,
				null);

		// Loop for every contact in the phone
		if (cursor.getCount() > 0) {

			while (cursor.moveToNext()) {

				String contact_id = cursor
						.getString(cursor.getColumnIndex(_ID));
				String name = cursor.getString(cursor
						.getColumnIndex(DISPLAY_NAME));

				int hasPhoneNumber = cursor.getInt(cursor
						.getColumnIndex(HAS_PHONE_NUMBER));

				if (hasPhoneNumber > 0) {

					// Query and loop for every phone number of the contact
					Cursor phoneCursor = contentResolver.query(
							PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?",
							new String[] { contact_id }, null);

					while (phoneCursor.moveToNext()) {
						phoneNumber = phoneCursor.getString(phoneCursor
								.getColumnIndex(NUMBER));

						mapContactDetails.put(phoneNumber.replaceAll(" ", ""),
								name);
					}

					phoneCursor.close();
				}

			}
		}

		return mapContactDetails;
	}
}
