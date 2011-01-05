package org.asoem.sico.utils;

import java.awt.Color;

//==============================================================================
//Rainbow Color SCALE Class
//
//Steve Pizer, UNC Chapel Hill (perceptually linearized)
//
//AGG - Alexander Gee
//
//041497 - created
//==============================================================================
public class Rainbow
{
	private static int size = 256;  // 256 defined RGB Colors

	private static Color[] rgb;

	private static Rainbow instance;

	public static Rainbow getInstance() {
		if(instance == null) {
			instance = new Rainbow();
		}
		return instance;
	}

	public Color[] createRainbow(int colors) {
		if (colors < 0 || colors > 256) {
			throw new IllegalArgumentException();
		}
		Color[] ret = new Color[colors];
		if(colors > 0) {
			int step = (int) Math.floor(size / colors);
			for (int i = 0; i < ret.length; i++) {
				ret[i] = rgb[i*step];
			}
		}
		return ret;
	}

	private Rainbow()
	{
		rgb = new Color[size];

		rgb[  0] = new Color(   0,   0,   0 );
		rgb[  1] = new Color(  45,   0,  36 );
		rgb[  2] = new Color(  56,   0,  46 );
		rgb[  3] = new Color(  60,   0,  49 );
		rgb[  4] = new Color(  67,   0,  54 );
		rgb[  5] = new Color(  70,   0,  59 );
		rgb[  6] = new Color(  71,   0,  61 );
		rgb[  7] = new Color(  75,   0,  68 );
		rgb[  8] = new Color(  74,   0,  73 );
		rgb[  9] = new Color(  74,   0,  77 );
		rgb[ 10] = new Color(  73,   0,  81 );
		rgb[ 11] = new Color(  71,   0,  87 );
		rgb[ 12] = new Color(  69,   1,  90 );
		rgb[ 13] = new Color(  68,   2,  94 );
		rgb[ 14] = new Color(  66,   3,  97 );
		rgb[ 15] = new Color(  63,   6, 102 );
		rgb[ 16] = new Color(  61,   7, 106 );
		rgb[ 17] = new Color(  58,  10, 109 );
		rgb[ 18] = new Color(  56,  12, 113 );
		rgb[ 19] = new Color(  53,  15, 116 );
		rgb[ 20] = new Color(  48,  18, 119 );
		rgb[ 21] = new Color(  47,  20, 121 );
		rgb[ 22] = new Color(  44,  23, 124 );
		rgb[ 23] = new Color(  41,  27, 128 );
		rgb[ 24] = new Color(  40,  28, 129 );
		rgb[ 25] = new Color(  37,  32, 132 );
		rgb[ 26] = new Color(  34,  36, 134 );
		rgb[ 27] = new Color(  29,  43, 137 );
		rgb[ 28] = new Color(  25,  52, 138 );
		rgb[ 29] = new Color(  24,  57, 139 );
		rgb[ 30] = new Color(  24,  62, 141 );
		rgb[ 31] = new Color(  24,  64, 142 );
		rgb[ 32] = new Color(  23,  65, 142 );
		rgb[ 33] = new Color(  23,  69, 143 );
		rgb[ 34] = new Color(  23,  71, 142 );
		rgb[ 35] = new Color(  23,  71, 142 );
		rgb[ 36] = new Color(  23,  73, 142 );
		rgb[ 37] = new Color(  23,  75, 142 );
		rgb[ 38] = new Color(  23,  75, 142 );
		rgb[ 39] = new Color(  23,  78, 142 );
		rgb[ 40] = new Color(  23,  80, 142 );
		rgb[ 41] = new Color(  23,  80, 142 );
		rgb[ 42] = new Color(  23,  82, 141 );
		rgb[ 43] = new Color(  23,  85, 141 );
		rgb[ 44] = new Color(  23,  85, 141 );
		rgb[ 45] = new Color(  23,  87, 140 );
		rgb[ 46] = new Color(  23,  87, 140 );
		rgb[ 47] = new Color(  24,  90, 140 );
		rgb[ 48] = new Color(  24,  90, 140 );
		rgb[ 49] = new Color(  24,  93, 139 );
		rgb[ 50] = new Color(  24,  93, 139 );
		rgb[ 51] = new Color(  24,  93, 139 );
		rgb[ 52] = new Color(  24,  93, 139 );
		rgb[ 53] = new Color(  24,  97, 139 );
		rgb[ 54] = new Color(  24,  97, 139 );
		rgb[ 55] = new Color(  25, 101, 138 );
		rgb[ 56] = new Color(  25, 101, 138 );
		rgb[ 57] = new Color(  25, 104, 137 );
		rgb[ 58] = new Color(  25, 104, 137 );
		rgb[ 59] = new Color(  25, 104, 137 );
		rgb[ 60] = new Color(  26, 108, 137 );
		rgb[ 61] = new Color(  26, 108, 137 );
		rgb[ 62] = new Color(  27, 111, 136 );
		rgb[ 63] = new Color(  27, 111, 136 );
		rgb[ 64] = new Color(  27, 111, 136 );
		rgb[ 65] = new Color(  27, 115, 135 );
		rgb[ 66] = new Color(  27, 115, 135 );
		rgb[ 67] = new Color(  28, 118, 134 );
		rgb[ 68] = new Color(  28, 118, 134 );
		rgb[ 69] = new Color(  29, 122, 133 );
		rgb[ 70] = new Color(  29, 122, 133 );
		rgb[ 71] = new Color(  29, 122, 133 );
		rgb[ 72] = new Color(  29, 122, 133 );
		rgb[ 73] = new Color(  29, 125, 132 );
		rgb[ 74] = new Color(  29, 125, 132 );
		rgb[ 75] = new Color(  30, 128, 131 );
		rgb[ 76] = new Color(  30, 128, 131 );
		rgb[ 77] = new Color(  31, 131, 130 );
		rgb[ 78] = new Color(  31, 131, 130 );
		rgb[ 79] = new Color(  31, 131, 130 );
		rgb[ 80] = new Color(  32, 134, 128 );
		rgb[ 81] = new Color(  32, 134, 128 );
		rgb[ 82] = new Color(  33, 137, 127 );
		rgb[ 83] = new Color(  33, 137, 127 );
		rgb[ 84] = new Color(  33, 137, 127 );
		rgb[ 85] = new Color(  34, 140, 125 );
		rgb[ 86] = new Color(  34, 140, 125 );
		rgb[ 87] = new Color(  35, 142, 123 );
		rgb[ 88] = new Color(  35, 142, 123 );
		rgb[ 89] = new Color(  36, 145, 121 );
		rgb[ 90] = new Color(  36, 145, 121 );
		rgb[ 91] = new Color(  36, 145, 121 );
		rgb[ 92] = new Color(  37, 147, 118 );
		rgb[ 93] = new Color(  37, 147, 118 );
		rgb[ 94] = new Color(  38, 150, 116 );
		rgb[ 95] = new Color(  38, 150, 116 );
		rgb[ 96] = new Color(  40, 152, 113 );
		rgb[ 97] = new Color(  40, 152, 113 );
		rgb[ 98] = new Color(  41, 154, 111 );
		rgb[ 99] = new Color(  41, 154, 111 );
		rgb[100] = new Color(  42, 156, 108 );
		rgb[101] = new Color(  42, 156, 108 );
		rgb[102] = new Color(  43, 158, 106 );
		rgb[103] = new Color(  43, 158, 106 );
		rgb[104] = new Color(  43, 158, 106 );
		rgb[105] = new Color(  45, 160, 104 );
		rgb[106] = new Color(  45, 160, 104 );
		rgb[107] = new Color(  46, 162, 101 );
		rgb[108] = new Color(  46, 162, 101 );
		rgb[109] = new Color(  48, 164,  99 );
		rgb[110] = new Color(  48, 164,  99 );
		rgb[111] = new Color(  50, 166,  97 );
		rgb[112] = new Color(  50, 166,  97 );
		rgb[113] = new Color(  51, 168,  95 );
		rgb[114] = new Color(  53, 170,  93 );
		rgb[115] = new Color(  53, 170,  93 );
		rgb[116] = new Color(  53, 170,  93 );
		rgb[117] = new Color(  55, 172,  91 );
		rgb[118] = new Color(  55, 172,  91 );
		rgb[119] = new Color(  57, 174,  88 );
		rgb[120] = new Color(  57, 174,  88 );
		rgb[121] = new Color(  59, 175,  86 );
		rgb[122] = new Color(  62, 177,  84 );
		rgb[123] = new Color(  64, 178,  82 );
		rgb[124] = new Color(  64, 178,  82 );
		rgb[125] = new Color(  67, 180,  80 );
		rgb[126] = new Color(  67, 180,  80 );
		rgb[127] = new Color(  69, 181,  79 );
		rgb[128] = new Color(  72, 183,  77 );
		rgb[129] = new Color(  72, 183,  77 );
		rgb[130] = new Color(  72, 183,  77 );
		rgb[131] = new Color(  75, 184,  76 );
		rgb[132] = new Color(  77, 186,  74 );
		rgb[133] = new Color(  80, 187,  73 );
		rgb[134] = new Color(  83, 189,  72 );
		rgb[135] = new Color(  87, 190,  72 );
		rgb[136] = new Color(  91, 191,  71 );
		rgb[137] = new Color(  95, 192,  70 );
		rgb[138] = new Color(  99, 193,  70 );
		rgb[139] = new Color( 103, 194,  70 );
		rgb[140] = new Color( 107, 195,  70 );
		rgb[141] = new Color( 111, 196,  70 );
		rgb[142] = new Color( 111, 196,  70 );
		rgb[143] = new Color( 115, 196,  70 );
		rgb[144] = new Color( 119, 197,  70 );
		rgb[145] = new Color( 123, 197,  70 );
		rgb[146] = new Color( 130, 198,  71 );
		rgb[147] = new Color( 133, 199,  71 );
		rgb[148] = new Color( 137, 199,  72 );
		rgb[149] = new Color( 140, 199,  72 );
		rgb[150] = new Color( 143, 199,  73 );
		rgb[151] = new Color( 143, 199,  73 );
		rgb[152] = new Color( 147, 199,  73 );
		rgb[153] = new Color( 150, 199,  74 );
		rgb[154] = new Color( 153, 199,  74 );
		rgb[155] = new Color( 156, 199,  75 );
		rgb[156] = new Color( 160, 200,  76 );
		rgb[157] = new Color( 167, 200,  78 );
		rgb[158] = new Color( 170, 200,  79 );
		rgb[159] = new Color( 173, 200,  79 );
		rgb[160] = new Color( 173, 200,  79 );
		rgb[161] = new Color( 177, 200,  80 );
		rgb[162] = new Color( 180, 200,  81 );
		rgb[163] = new Color( 183, 199,  82 );
		rgb[164] = new Color( 186, 199,  82 );
		rgb[165] = new Color( 190, 199,  83 );
		rgb[166] = new Color( 196, 199,  85 );
		rgb[167] = new Color( 199, 198,  85 );
		rgb[168] = new Color( 199, 198,  85 );
		rgb[169] = new Color( 203, 198,  86 );
		rgb[170] = new Color( 206, 197,  87 );
		rgb[171] = new Color( 212, 197,  89 );
		rgb[172] = new Color( 215, 196,  90 );
		rgb[173] = new Color( 218, 195,  91 );
		rgb[174] = new Color( 224, 194,  94 );
		rgb[175] = new Color( 224, 194,  94 );
		rgb[176] = new Color( 230, 193,  96 );
		rgb[177] = new Color( 233, 192,  98 );
		rgb[178] = new Color( 236, 190, 100 );
		rgb[179] = new Color( 238, 189, 104 );
		rgb[180] = new Color( 240, 188, 106 );
		rgb[181] = new Color( 240, 188, 106 );
		rgb[182] = new Color( 242, 187, 110 );
		rgb[183] = new Color( 244, 185, 114 );
		rgb[184] = new Color( 245, 184, 116 );
		rgb[185] = new Color( 247, 183, 120 );
		rgb[186] = new Color( 248, 182, 123 );
		rgb[187] = new Color( 248, 182, 123 );
		rgb[188] = new Color( 250, 181, 125 );
		rgb[189] = new Color( 251, 180, 128 );
		rgb[190] = new Color( 252, 180, 130 );
		rgb[191] = new Color( 253, 180, 133 );
		rgb[192] = new Color( 253, 180, 133 );
		rgb[193] = new Color( 254, 180, 134 );
		rgb[194] = new Color( 254, 179, 138 );
		rgb[195] = new Color( 255, 179, 142 );
		rgb[196] = new Color( 255, 179, 145 );
		rgb[197] = new Color( 255, 179, 145 );
		rgb[198] = new Color( 255, 179, 152 );
		rgb[199] = new Color( 255, 180, 161 );
		rgb[200] = new Color( 255, 180, 164 );
		rgb[201] = new Color( 255, 180, 167 );
		rgb[202] = new Color( 255, 180, 167 );
		rgb[203] = new Color( 255, 181, 169 );
		rgb[204] = new Color( 255, 181, 170 );
		rgb[205] = new Color( 255, 182, 173 );
		rgb[206] = new Color( 255, 183, 176 );
		rgb[207] = new Color( 255, 183, 176 );
		rgb[208] = new Color( 255, 184, 179 );
		rgb[209] = new Color( 255, 185, 179 );
		rgb[210] = new Color( 255, 185, 182 );
		rgb[211] = new Color( 255, 186, 182 );
		rgb[212] = new Color( 255, 186, 182 );
		rgb[213] = new Color( 255, 187, 185 );
		rgb[214] = new Color( 255, 188, 185 );
		rgb[215] = new Color( 255, 189, 188 );
		rgb[216] = new Color( 255, 189, 188 );
		rgb[217] = new Color( 255, 190, 188 );
		rgb[218] = new Color( 255, 191, 191 );
		rgb[219] = new Color( 255, 192, 191 );
		rgb[220] = new Color( 255, 194, 194 );
		rgb[221] = new Color( 255, 194, 194 );
		rgb[222] = new Color( 255, 197, 197 );
		rgb[223] = new Color( 255, 198, 198 );
		rgb[224] = new Color( 255, 200, 200 );
		rgb[225] = new Color( 255, 201, 201 );
		rgb[226] = new Color( 255, 201, 201 );
		rgb[227] = new Color( 255, 202, 202 );
		rgb[228] = new Color( 255, 203, 203 );
		rgb[229] = new Color( 255, 205, 205 );
		rgb[230] = new Color( 255, 206, 206 );
		rgb[231] = new Color( 255, 206, 206 );
		rgb[232] = new Color( 255, 208, 208 );
		rgb[233] = new Color( 255, 209, 209 );
		rgb[234] = new Color( 255, 211, 211 );
		rgb[235] = new Color( 255, 215, 215 );
		rgb[236] = new Color( 255, 216, 216 );
		rgb[237] = new Color( 255, 216, 216 );
		rgb[238] = new Color( 255, 218, 218 );
		rgb[239] = new Color( 255, 219, 219 );
		rgb[240] = new Color( 255, 221, 221 );
		rgb[241] = new Color( 255, 223, 223 );
		rgb[242] = new Color( 255, 226, 226 );
		rgb[243] = new Color( 255, 228, 228 );
		rgb[244] = new Color( 255, 230, 230 );
		rgb[245] = new Color( 255, 230, 230 );
		rgb[246] = new Color( 255, 232, 232 );
		rgb[247] = new Color( 255, 235, 235 );
		rgb[248] = new Color( 255, 237, 237 );
		rgb[249] = new Color( 255, 240, 240 );
		rgb[250] = new Color( 255, 243, 243 );
		rgb[251] = new Color( 255, 246, 246 );
		rgb[252] = new Color( 255, 249, 249 );
		rgb[253] = new Color( 255, 251, 251 );
		rgb[254] = new Color( 255, 253, 253 );
		rgb[255] = new Color( 255, 255, 255 );
	}
}

