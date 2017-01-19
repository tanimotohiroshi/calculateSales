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


		if (args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		/* 支店別売上額合計を保持するマップ */
		HashMap<String, String> branchNameMap = new HashMap<>();
		HashMap<String, Long> branchSalesMap = new HashMap<>();

		String branchLstFilePath = args[0] + File.separator + "branch.lst";
		if (!readingFile (branchLstFilePath, "支店", "^\\d{3}$", branchNameMap, branchSalesMap)){
			return;
		}

		/* 商品別売上合計を保持するマップ */
		HashMap<String, String> commodityNameMap = new HashMap<>();
		HashMap<String, Long> commoditySalesMap = new HashMap<>();

		String commodityLstFilePath = new String(args[0] + File.separator + "commodity.lst");
		if (!readingFile (commodityLstFilePath, "商品", "[0-9a-zA-Z]{8}",
				commodityNameMap, commoditySalesMap)){
			return;
		}
		/* 売上ファイルの選別 */
		/* 8桁で.rcdのファイルをarrayListに格納 */

		File[] files = new File(args[0]).listFiles();
		ArrayList<File> salesFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++){
			File file = files[i];
			if (files[i].isFile()){
				if (file.getName().matches("^[0-9]{8}.rcd$")){
					salesFiles.add(file);
				}
			}
		}

		/* 連番でなければ終了 */
		for (int i = 0; i < salesFiles.size(); i++){
			String str = salesFiles.get(i).getName().substring(1, 8);
			int comparison = Integer.parseInt(str);
			if (i + 1 != comparison){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		Long branchSum;/* 支店別売上額合計 */
		Long commoditySum;/* 商品別売上額合計 */
		for (int i = 0; i < salesFiles.size(); i++) {

			BufferedReader br = null;
			try {
				FileReader fr = new FileReader(salesFiles.get(i));
				br = new BufferedReader(fr);

				ArrayList<String> salesFile = new ArrayList<String>();

				String str;
				while ((str = br.readLine()) != null){
					salesFile.add(str);
				}

				if (salesFile.size() != 3){
					System.out.println(salesFiles.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				if (!branchNameMap.containsKey(salesFile.get(0))){
					System.out.println(salesFiles.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if (!commodityNameMap.containsKey(salesFile.get(1))){
					System.out.println(salesFiles.get(i).getName() + "の商品コードが不正です");
					return;
				}

				if (!salesFile.get(2).matches("[0-9]{1,10}")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				/* この4行で支店コード別売上高合計 */

				long checkSum;

				long branchSales = Long.parseLong(salesFile.get(2));
				long branchValue = branchSalesMap.get(salesFile.get(0));
				branchSum = branchSales + branchValue;
				checkSum = String.valueOf(branchSum).length();
				if (checkSum > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSalesMap.put(salesFile.get(0), branchSum);

				/* この4行で商品コード別売上高合計 */

				long commoditySales = Long.parseLong(salesFile.get(2));
				long commodityValue = commoditySalesMap.get(salesFile.get(1));
				commoditySum = commoditySales + commodityValue;
				checkSum = String.valueOf(commoditySum).length();
				if (checkSum > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				commoditySalesMap.put(salesFile.get(1), commoditySum);

			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
			}finally{
				try {
					if (br != null){
						br.close();
					}
				}catch(IOException e){
					System.out.println("予期せぬエラーが表示されました");
				}
			}
		}

		/* 支店別集計ファイルの作成、書き込み */
		String branchOutFilePath = args[0] + File.separator + "branch.out";
		if (!writingFile (branchOutFilePath, branchNameMap, branchSalesMap)){
			return;
		}
		/*商品別ファイルの作成、書き込み*/
		String commodityOutFilePath = args[0] + File.separator + "commodity.out";
		if(!writingFile (commodityOutFilePath, commodityNameMap, commoditySalesMap)){
			return;
		}
	}
	/*ファイル書き出しメソッド*/

	public static boolean writingFile(String path,
			HashMap<String, String> mapName, HashMap<String, Long> mapNameSalesMap){

		File SummaryFiles = new File(path);
		String Separate = System.getProperty("line.separator");

		List<Map.Entry<String, Long>> salesMap = new ArrayList<>(mapNameSalesMap.entrySet());
		Collections.sort(salesMap, new Comparator<Map.Entry<String, Long>>() {
			@Override
			public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2){
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		try{
			SummaryFiles.createNewFile();
			FileWriter filewriter = new FileWriter(SummaryFiles);
			for (Map.Entry<String, Long> e : salesMap) {
				filewriter.write(e.getKey() + "," + mapName.get(e.getKey()) + "," + e.getValue() + Separate);
			}
			filewriter.close();
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		return true;
	}




			/*ファイル読み出しメソッド*/
	public static boolean readingFile(String path , String name, String code,
			HashMap<String, String> nameMap, HashMap<String, Long> salesMap){

		BufferedReader br = null;
		try {
			File File = new File(path);
			FileReader filereader = new FileReader(File);
			br = new BufferedReader(filereader);
			String str;
			while ((str = br.readLine()) != null){
				String[] element = str.split(",", 0);
				if (2 != element.length){
					System.out.println(name + "定義ファイルのフォーマットが不正です");
					return false;
				}
				if (!element[0].matches(code)){
					System.out.println(name + "定義ファイルのフォーマットが不正です");
					return false;
				}
				nameMap.put(element[0], element[1]);
				salesMap.put(element[0], 0L);
			}
		}catch(IOException e){
			System.out.println(name + "定義ファイルが存在しません");
			return false;

		}finally{
			try {
				if (br != null){
					br.close();
				}
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
			}
		}
		return true;
	}
}