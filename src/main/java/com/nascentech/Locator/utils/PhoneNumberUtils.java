package com.nascentech.Locator.utils;

public class PhoneNumberUtils {
	public static final char PAUSE = ',';
	public static final char WAIT = ';';
	public static final char WILD = 'N';
	static final int MIN_MATCH = 5;

	public static boolean compare(String a, String b) {
		int ia, ib;
		int matched;
		if (a == null || b == null)
			return a == b;
		if (a.length() == 0 || b.length() == 0) {
			return false;
		}
		ia = indexOfLastNetworkChar(a);
		ib = indexOfLastNetworkChar(b);
		matched = 0;
		while (ia >= 0 && ib >= 0) {
			char ca, cb;
			boolean skipCmp = false;
			ca = a.charAt(ia);
			if (!isDialable(ca)) {
				ia--;
				skipCmp = true;
			}
			cb = b.charAt(ib);
			if (!isDialable(cb)) {
				ib--;
				skipCmp = true;
			}
			if (!skipCmp) {
				if (cb != ca && ca != WILD && cb != WILD) {
					break;
				}
				ia--;
				ib--;
				matched++;
			}
		}
		if (matched < MIN_MATCH) {
			int aLen = a.length();

			if (aLen == b.length() && aLen == matched) {
				return true;
			}
			return false;
		}
		if (matched >= MIN_MATCH && (ia < 0 || ib < 0)) {
			return true;
		}

		if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
			return true;
		}
		if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
			return true;
		}
		if (matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1)) {
			return true;
		}
		return false;
	}

	private static boolean matchIntlPrefix(String a, int len) {
		int state = 0;
		for (int i = 0; i < len; i++) {
			char c = a.charAt(i);
			switch (state) {
			case 0:
				if (c == '+')
					state = 1;
				else if (c == '0')
					state = 2;
				else if (isNonSeparator(c))
					return false;
				break;

			case 2:
				if (c == '0')
					state = 3;
				else if (c == '1')
					state = 4;
				else if (isNonSeparator(c))
					return false;
				break;

			case 4:
				if (c == '1')
					state = 5;
				else if (isNonSeparator(c))
					return false;
				break;

			default:
				if (isNonSeparator(c))
					return false;
				break;
			}
		}
		return state == 1 || state == 3 || state == 5;
	}

	public final static boolean isNonSeparator(char c) {
		return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD || c == WAIT || c == PAUSE;
	}

	static private int indexOfLastNetworkChar(String a) {
		int pIndex, wIndex;
		int origLength;
		int trimIndex;
		origLength = a.length();
		pIndex = a.indexOf(PAUSE);
		wIndex = a.indexOf(WAIT);
		trimIndex = minPositive(pIndex, wIndex);
		if (trimIndex < 0) {
			return origLength - 1;
		} else {
			return trimIndex - 1;
		}
	}

	static private int minPositive(int a, int b) {
		if (a >= 0 && b >= 0) {
			return (a < b) ? a : b;
		} else if (a >= 0) {
			return a;
		} else if (b >= 0) {
			return b;
		} else {
			return -1;
		}
	}

	private static boolean matchTrunkPrefix(String a, int len) {
		boolean found;
		found = false;
		for (int i = 0; i < len; i++) {
			char c = a.charAt(i);
			if (c == '0' && !found) {
				found = true;
			} else if (isNonSeparator(c)) {
				return false;
			}
		}
		return found;
	}

	private static boolean matchIntlPrefixAndCC(String a, int len) {
		int state = 0;
		for (int i = 0; i < len; i++) {
			char c = a.charAt(i);
			switch (state) {
			case 0:
				if (c == '+')
					state = 1;
				else if (c == '0')
					state = 2;
				else if (isNonSeparator(c))
					return false;
				break;

			case 2:
				if (c == '0')
					state = 3;
				else if (c == '1')
					state = 4;
				else if (isNonSeparator(c))
					return false;
				break;

			case 4:
				if (c == '1')
					state = 5;
				else if (isNonSeparator(c))
					return false;
				break;

			case 1:
			case 3:
			case 5:
				if (isISODigit(c))
					state = 6;
				else if (isNonSeparator(c))
					return false;
				break;

			case 6:
			case 7:
				if (isISODigit(c))
					state++;
				else if (isNonSeparator(c))
					return false;
				break;
			default:
				if (isNonSeparator(c))
					return false;
			}
		}
		return state == 6 || state == 7 || state == 8;
	}

	public static boolean isISODigit(char c) {
		return c >= '0' && c <= '9';
	}

	public final static boolean isDialable(char c) {
		return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
	}
}
