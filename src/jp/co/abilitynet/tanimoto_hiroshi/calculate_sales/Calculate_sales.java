package jp.co.abilitynet.tanimoto_hiroshi.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculate_sales {


	public static void main(String[] args) {

		/* コマンドライン引数が1つあるかどうか */


		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		/* 支店定義ファイル */

		/* 支店別売上額合計を保持するマップ */
		HashMap<String, String> branchNameMap = new HashMap<>();
		HashMap<String, Long> branchSalesMap = new HashMap<>();



		if(!readingFile( args[0], "branch.lst" , "支店" , "^\\d{3}$" , branchNameMap,branchSalesMap )){
			return;
		}


		/* 支店定義ファイルの読み込み、マップ作成終了 */

		/* 商品定義ファイル */

		/* 商品別売上合計を保持するマップ */
		HashMap<String, String> commodityNameMap = new HashMap<>();
		HashMap<String, Long> commoditySalesMap = new HashMap<>();

		if(!readingFile( args[0] , "commodity.lst" , "商品" , "[0-9a-zA-Z]{8}" ,
				commodityNameMap, commoditySalesMap) ){
			return;
		}



		/* 商品定義ファイルの読み込み、マップ作製 */

		/* 売上ファイルの選別 */
		/* 8桁で.rcdのファイルをarrayListに格納 */

		File[] files1 = new File(args[0]).listFiles();
		ArrayList<File> salesFiles = new ArrayList<>();

		for (int i = 0; i < files1.length; i++) {
			File file = files1[i];
			if (files1[i].isFile()) {
				if (file.getName().matches("^[0-9]{8}.rcd$")) {
					salesFiles.add(file);
				}
			}
		}

		/* 連番でなければ終了 */
		for (int i = 0; i < salesFiles.size(); i++) {
			String str = salesFiles.get(i).getName().substring(1, 8);
			int d = Integer.parseInt(str);
			if (i + 1 != d) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		Long branchSum;/* 支店別売上額合計 */
		Long commoditySum;/* 商品別売上額合計 */
		for (int i = 0; i < salesFiles.size(); i++) {

			BufferedReader br = null;
			try {


				salesFiles.get(i).getName();

				FileReader fr = new FileReader(salesFiles.get(i));
				br = new BufferedReader(fr);

				ArrayList<String> array = new ArrayList<String>();

				String str;
				while ((str = br.readLine()) != null) {
					array.add(str);
				}

				if (array.size() != 3) {
					System.out.println(salesFiles.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				if (!branchNameMap.containsKey(array.get(0))) {
					System.out.println(salesFiles.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if (!commodityNameMap.containsKey(array.get(1))) {
					System.out.println(salesFiles.get(i).getName() + "の商品コードが不正です");
					return;
				}

				if (!array.get(2).matches("[0-9]{1,10}")) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				/* この4行で支店コード別売上高合計 */

				long branchSales = Long.parseLong(array.get(2));
				long branchValue = branchSalesMap.get(array.get(0));
				branchSum = branchSales + branchValue;
				branchSalesMap.put(array.get(0), branchSum);
				long checkSum1 = String.valueOf(branchSum).length();

				if (checkSum1 > 10 ) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				/* この4行で商品コード別売上高合計 */

				long commoditySales = Long.parseLong(array.get(2));
				long commodityValue = commoditySalesMap.get(array.get(1));
				commoditySum = commoditySales + commodityValue;
				commoditySalesMap.put(array.get(1), commoditySum);
				long checkSum2 = String.valueOf(commoditySum).length();


				if (checkSum2 > 10 ) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					System.out.println("予期せぬエラーが表示されました");
				}
			}
		}

		/* 支店別集計ファイルの作成、書き込み */
		if (!writingFile( args[0] , "branch.out" , branchNameMap , branchSalesMap )){
			return;
		}


		/*商品別ファイルの作成、書き込み*/

		if(!writingFile( args[0] , "commodity.out" , commodityNameMap , commoditySalesMap )){
			return;
		}
	}

	/*ファイル書き出しメソッド*/

	public static boolean writingFile(String path , String fileName ,
			HashMap<String, String> mapName , HashMap<String , Long >mapNameSalesMap){


		File SummaryFiles = new File( path , fileName );
		String Separate = System.getProperty("line.separator");

		try {
			SummaryFiles.createNewFile();
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false ;
		}
		List<Map.Entry<String, Long>> salesMap2 = new ArrayList<>( mapNameSalesMap.entrySet());
		Collections.sort(salesMap2, new Comparator<Map.Entry<String, Long>>() {
			@Override
			public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		try {
			FileWriter filewriter = new FileWriter(SummaryFiles);
			for (Map.Entry<String, Long> e : salesMap2) {
				filewriter.write(e.getKey() + "," + mapName.get(e.getKey()) + "," + e.getValue() + Separate);
			}
			filewriter.close();
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		return true;
	}




			/*ファイル読み出しメソッド*/
	public static boolean readingFile(String path, String filename, String name, String code,
			HashMap<String, String> nameMap, HashMap<String, Long> salesMap) {

		BufferedReader br = null;

		try {

			File File = new File(path, filename);
			FileReader filereader = new FileReader( File );
			br  = new BufferedReader( filereader );
			String str;
			while ((str = br.readLine()) != null) {
				String[] element = str.split(",", 0);
				if (2 != element.length) {
					System.out.println( name + "定義ファイルのフォーマットが不正です");
					return false;
				}
				if (!element[0].matches(code)) {
					System.out.println( name + "定義ファイルのフォーマットが不正です");
					return false;
				}
				nameMap.put(element[0], element[1]);
				salesMap.put(element[0], 0L);
			}
		} catch (IOException e) {
			System.out.println( name + "定義ファイルが存在しません");
			return false;

		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
		}
		return true;
	}
}