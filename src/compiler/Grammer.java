package compiler;

import java.util.*;

class Identifier {
	public boolean isTerminal;
	public String string;
	public HashMap<Identifier,Identifier> first = new HashMap<Identifier,Identifier>();
	public HashMap<Identifier,Identifier> follow = new HashMap<Identifier,Identifier>();
	Identifier(boolean isTerminal, String string) {
		this.isTerminal = isTerminal;
		this.string = string;
		if (isTerminal)
			this.first.put(this, this);
	}
	int checkRound = 0;
	public boolean equals(Object o) {
		if (o instanceof Identifier)
			return ((Identifier)o).string.equals(string);
		else return false;
	}
	public int hashCode() {
		return string.hashCode();
	}
	public int hashCode1() {
		return super.hashCode();
	}
	public String toString() {
		return string;
	}
	public static void getFirst() {
		boolean changed;
		ArrayList<Expression> exp = Grammer.exp;
		do {
			changed = false;
			Iterator<Expression> it = exp.iterator();
			while (it.hasNext()) {
				Expression temp = it.next();
				Identifier templ = temp.left;
				//Identifier tempr = temp.right.get(0);
				/*
				if (tempr.isTerminal && !tempr.string.equals("e")) {
					if (templ.first.put(tempr,tempr) == null)
						changed = true;
				}
				else if (tempr.isTerminal && tempr.string.equals("e")) {*/
					boolean hasE;
					if (temp.left.first.put(Grammer.e, Grammer.e) == null)
						hasE = false;
					else hasE = true;
					
					for (int i = 0; i < temp.right.size(); i++) {
						boolean checkNext = false;
						if (temp.right.get(i).isTerminal && temp.right.get(i).string.equals("e")) {
							//if (!templ.first.keySet().containsAll(temp.right.get(i + 1).first.keySet())) {
							//	changed = true;
							//	templ.first.putAll(temp.right.get(i + 1).first);
							//}
							checkNext = true;
						}
						else if (temp.right.get(i).isTerminal && !temp.right.get(i).string.equals("e")) {
							if (templ.first.put(temp.right.get(i), temp.right.get(i)) == null) {
								changed = true;
							}
						}
						else if (!temp.right.get(i).isTerminal) {
							if (!templ.first.keySet().containsAll(temp.right.get(i).first.keySet())) {
								changed = true;
								templ.first.putAll(temp.right.get(i).first);
							}
							if (temp.right.get(i).first.containsKey(Grammer.e))
								checkNext = true;
						}
						if (i == temp.right.size() - 1) {
							if (checkNext && !hasE) 
								changed = true;
							else if (!checkNext && !hasE)
								temp.left.first.remove(Grammer.e);
						}
						else if (!checkNext) {
							if (!hasE)
								temp.left.first.remove(Grammer.e);
							break;
						}
					}
					
					
					
					
					/*
					//只有e
					if (temp.right.size() == 1) {
						if (templ.first.put(tempr,tempr) == null)
							changed = true;
					}
					else {
						if (!templ.first.keySet().containsAll(temp.right.get(1).first.keySet())) {
							changed = true;
							templ.first.putAll(temp.right.get(1).first);
						}
					}
				}
				else if (!tempr.isTerminal) {
					if (!templ.first.keySet().containsAll(tempr.first.keySet())) {
						templ.first.putAll(tempr.first);
						changed = true;
					}
				}*/				
			}
		} while (changed);
	}
	public static void getFollow() {
		boolean changed;
		int round = 1;
		ArrayList<Expression> exp = Grammer.exp;
		do {
			changed = false;
			Iterator<Expression> it = exp.iterator();
			while (it.hasNext()) {
				Identifier temp = it.next().left;
				//跳过本轮重复检查
				if (!temp.isTerminal && temp.checkRound < round) {
					temp.checkRound++;
					//是S则添加$
					if (temp == Grammer.S)  {
						if (temp.follow.put(Grammer.dollar,Grammer.dollar) == null)
							changed = true;
					}
					//遍历寻找表达式右边的非终结符
					Iterator<Expression> tempi = exp.iterator();
					while (tempi.hasNext()) {
						Expression etemp = tempi.next();
						//存在非终结符
						if (etemp.right.contains(temp)) {
							for (int i = 0; i < etemp.right.size(); i++) {
								//找到了当前非终结符
								if (etemp.right.get(i).equals(temp)) {
									//下一个不含有e也不是最后一个
									if (i != etemp.right.size() - 1 && !etemp.right.get(i + 1).first.containsKey(Grammer.e)) {
										if (!temp.follow.keySet().containsAll(etemp.right.get(i + 1).first.keySet())) {
											temp.follow.putAll(etemp.right.get(i + 1).first);
											changed = true;
										}
									}
									else if (i == etemp.right.size() - 1){
										if (!temp.follow.keySet().containsAll(etemp.left.follow.keySet())) {
											temp.follow.putAll(etemp.left.follow);
											changed = true;
										}
									}
									else {
										temp.follow.put(Grammer.e, Grammer.e);
										if (!temp.follow.keySet().containsAll(etemp.right.get(i + 1).first.keySet())) {
											temp.follow.putAll(etemp.right.get(i + 1).first);
											changed = true;
										}
										temp.follow.remove(Grammer.e);
										if (!temp.follow.keySet().containsAll(etemp.left.follow.keySet())) {
											temp.follow.putAll(etemp.left.follow);
											changed = true;
										}
									}
								}
							}
						}
					}
				}
			}
			round++;
		} while(changed);
		//清零
		Iterator<Identifier> it = Grammer.iset.keySet().iterator();
		while (it.hasNext())
			it.next().checkRound = 0;
	}
}

class Expression {
	public Identifier left;
	public ArrayList<Identifier> right = new ArrayList<Identifier>();
	public Identifier getFirstIdentifier() {
		if (right.size() != 0)
			return right.get(0);
		else return null;
	}
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(left.string + "->");
		Iterator<Identifier> it = right.iterator();
		while (it.hasNext())
			s.append(it.next().string);
		return s.toString();
	}
	public HashMap<Identifier,Identifier> getFirst() {//用于对特定右部分析
		HashMap<Identifier,Identifier> returnResult = new HashMap<Identifier,Identifier>();
		boolean hasE;
		if (returnResult.put(Grammer.e, Grammer.e) == null)
			hasE = false;
		else hasE = true;
		
		for (int i = 0; i < this.right.size(); i++) {
			boolean checkNext = false;
			if (this.right.get(i).isTerminal && this.right.get(i).string.equals("e")) {
				checkNext = true;
			}
			else if (this.right.get(i).isTerminal && !this.right.get(i).string.equals("e")) 
				returnResult.put(this.right.get(i), this.right.get(i));
			else if (!this.right.get(i).isTerminal) {
				returnResult.putAll(this.right.get(i).first);
				if (this.right.get(i).first.containsKey(Grammer.e))
					checkNext = true;
			}
			//if (i == this.right.size() - 1) {
			//	if (!checkNext && !hasE)
			//		returnResult.remove(Grammer.e);
			//}
			if (!checkNext) {
				if (!hasE)
					returnResult.remove(Grammer.e);
				break;
			}
		}
		return returnResult;
	}
}

class result implements Comparable<result>{
	Identifier nonterminal;
	Identifier terminal;
	Expression result;
	
	public result(Identifier nonterminal, Identifier terminal, Expression result) {
		this.nonterminal = nonterminal;
		this.terminal = terminal;
		this.result = result;
	}
	
	public String toString() {
		return "(" + nonterminal.string + "," + terminal.string + "):" + result;
	}

	@Override
	public int compareTo(result o) {
		return nonterminal.string.compareTo(o.nonterminal.string);
	}
	
	public boolean equals(Object o) {
		return o instanceof result && nonterminal == ((result)o).nonterminal && terminal == ((result)o).terminal;
	}
}

public class Grammer {
	public static HashMap<Identifier,Identifier> iset = new HashMap<Identifier,Identifier>();
	public static ArrayList<Expression> exp = new ArrayList<Expression>();
	public static Identifier S;
	public static Identifier e = new Identifier(true,"e");
	public static Identifier dollar = new Identifier(true,"$");
	
	public static boolean nonrecursivePredictiveAnalysis(ArrayList<Identifier> in, ArrayList<result> sheet) {
		in.add(dollar);
		Stack<Identifier> st = new Stack<Identifier>();
		st.push(dollar);
		st.push(S);
		Iterator<Identifier> ip = in.iterator();
		Identifier X;//令X为栈顶文法符号
		Identifier a = ip.next();//a是ip所指向的输入符号
		StringBuilder leftSentence = new StringBuilder();
		do {
			X = st.peek();
			
			if (X.isTerminal|| X == dollar) {
				if (X == a) {
					leftSentence.append(X);
					st.pop();
					X = st.peek();
					a = ip.next();
				}
				else return false;
			}
			else {
				result rtemp = new result(X,a,null);
				if (sheet.contains(rtemp)) {
					Expression etemp = sheet.get(sheet.indexOf(rtemp)).result;
					st.pop();
					X = st.peek();
					ArrayList<Identifier> right = (ArrayList<Identifier>) etemp.right.clone();
					Collections.reverse(right);
					Iterator<Identifier> iit = right.iterator();
					while (iit.hasNext()) {
						Identifier tempi = iit.next();
						if (tempi != e)
							st.push(tempi);
					}
					X = st.peek();
					System.out.println("利用" + etemp.toString() + "消除");
				}
				else return false;
			}
			System.out.print("->" + leftSentence + '.');
			List<Identifier> outStack = (List<Identifier>) st.clone();
			Collections.reverse(outStack);
			Iterator<Identifier> it = outStack.iterator();
			while (it.hasNext())
				System.out.print(it.next().string);
			System.out.println();
		} while (X!=dollar);
		return true;
	}
	
	public static ArrayList<result> predictingAnalysisSheet() {
		//建立分析表
		ArrayList<result> sheet = new ArrayList<result>();
		//对文法G的每个产生式A-alpha
		Iterator<Expression> eit = exp.iterator();
		while (eit.hasNext()) {
			Expression etemp = eit.next();
			HashMap<Identifier,Identifier> fakeFirst = etemp.getFirst();
			//对每个a属于FIRST(alpha)，把A-alpha放到M[A,a]中
			{
				Iterator<Identifier> iit = fakeFirst.keySet().iterator();//etemp.right.get(0).first.keySet().iterator();
				while (iit.hasNext()) {
					Identifier itemp = iit.next();//a
					if (itemp != e) {
						result temp = new result(etemp.left,itemp,etemp);
						if (!sheet.contains(temp))
							sheet.add(temp);
						else {
							System.out.println("有重复表项，无法进一步分析，请提取左公因子再试一次！");
							System.exit(1);
						}
					}
				}
			}
			{
				if (fakeFirst.containsKey((Identifier)(Grammer.e))) {
					//对于任何b属于FOLLOW(A)，把A-alpha放入M[A,b]中
					Iterator<Identifier> iit = etemp.left.follow.keySet().iterator();
					while (iit.hasNext()) {
						Identifier itemp = iit.next();//b
						result temp = new result(etemp.left,itemp,etemp);
						if (!sheet.contains(temp))
							sheet.add(temp);
						else {
							System.out.println("有重复表项，无法进一步分析，请提取左公因子再试一次！");
							System.exit(1);
						}
						
					}
				}
			}
		}
		//打印
		Iterator<result> it = sheet.iterator();
		System.out.println("分析表如下：");
		while (it.hasNext())
			System.out.println(it.next());
		return sheet;
	}
	
	public static void main(String[] args) {
		//无转义字符：->和|，非终结符大写字母开头，e为空，第一条表达式非终结符为S
		System.out.println("欢迎使用LL(1)文法分析程序！\nMade by 沐晓枫\n请您输入LL(1)文法，每一行一条，一条中只能有一个左部非终结符（大写字母开头），右侧符号之间需空格隔开以便分词。\n推出符号为->或者符号为|\n输入空行代表输入结束。\n示例输入:\nE->T E'\nE'->+ T E'|- T E'|e\nT->F T'\nT'->* F T'|/ F T'|e\nF->( E )|Num\nNum->0|1|2|3|4|5|6|7|8|9\n请输入文法:\n");
		Scanner input = new Scanner(System.in);
		iset.put(e, e);
		iset.put(dollar, dollar);
		boolean init = true;
		while (true) {
			String buffer = input.nextLine();
			if (buffer.length() == 0)//空行结束
				break;
			else {
				String[] fisrtSplit = buffer.split("->");
				if (fisrtSplit.length == 2) {//多个-号
					String[] secondSplit = fisrtSplit[0].split(" ");
					if (secondSplit.length != 1 || !(secondSplit[0].length() != 0 && secondSplit[0].charAt(0) >= 'A' && secondSplit[0].charAt(0) <= 'Z'))//左边多个符号或者左面终结符
						System.out.println("错误：非CFG！");
					else {
						//添加数据
						Identifier left = new Identifier(false,secondSplit[0]);
						if (iset.get(left) == null)
							iset.put(left,left);
						else left = iset.get(left);
						if (init)
							S = left;
						Identifier tempi;
						secondSplit = fisrtSplit[1].split("\\|");
						for (int m = 0; m < secondSplit.length; m++) {
							String[] thirdSplit = secondSplit[m].split(" ");
							boolean valid = false;
							Expression temp = new Expression();
							for (int i = 0; i < thirdSplit.length; i++) {//对每一个符号处理
								if (thirdSplit[i].length() != 0) {//忽略空格
									valid = true;
									temp.left = left;
									if (thirdSplit[i].charAt(0) >= 'A' && thirdSplit[i].charAt(0) <= 'Z') //非终结符
										tempi = new Identifier(false, thirdSplit[i]);
									else tempi = new Identifier(true, thirdSplit[i]);
									if (iset.get(tempi) == null)
										iset.put(tempi, tempi);
									else tempi = iset.get(tempi);
									temp.right.add(tempi);
									
								}
							}
							if (valid) {//有效的expression
								exp.add(temp);
								if (temp.left.string.equals(temp.right.get(0).string)) {
									System.out.println("发现左递归，请消除后再试！");
									System.exit(2);
								}
							}
							else System.out.println("无效的表达式");
						}
					}
				}
				else System.out.println("错误：发现多个->！");
			}
			init = false;
		}
		Identifier.getFirst();
		Identifier.getFollow();
		
		ArrayList<result> result = predictingAnalysisSheet();
		//算法4.1
		System.out.print("\n示例输入:( ( ( 2 + 4 ) * 8 ) / 5 ) + 0 )\n(算法4.1)输入待分析符号串w(空格分割每个符号):");
		String temp = input.nextLine();
		String[] i = temp.split(" ");
		ArrayList<Identifier> in = new ArrayList<Identifier>();
		for (int j = 0; j < i.length; j++) {
			if (i[j].charAt(0) >= 'A' && i[j].charAt(0) <= 'Z') {
				Identifier tempi = new Identifier(false,i[j]);
				in.add(iset.get(tempi));
			}
			else {
				Identifier tempi = new Identifier(true,i[j]);
				in.add(iset.get(tempi));
			}
		}
		//开始分析
		try {
			if (!nonrecursivePredictiveAnalysis(in, result))
				System.out.println("不能推出产生式");
		} catch (Exception ex) {
			System.out.println("不能推出产生式");
		}
	}
}
