package compiler2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

class pair implements Comparable<pair>{
	ItemCollection ic;
	Identifier i;
	
	pair(ItemCollection ic,	Identifier i) {
		this.i = i;
		this.ic = ic;
	}
	
	public boolean equals(Object o) {
		if (o instanceof pair) {
			pair oo = (pair)o;
			return ic == oo.ic && i == oo.i;
		}
		else return false;
	}
	
	public int hashCode() {
		//System.out.println((ic.hashCode()+i.hashCode()) + "\t" + ic.id + " " + i);
		return ic.hashCode()+i.hashCode();
		//return 1;
	}
	
	public String toString() {
		return "(" + ic.id + "," + i.string + "):";
	}
	
	public int compareTo(pair o) {
		return ic.id > o.ic.id ? 1 : 0;
	}
}

public class Grammar {
	static Scanner input = new Scanner(System.in);
	public static HashMap<Identifier,Identifier> iset = new HashMap<Identifier,Identifier>();
	public static ArrayList<Expression> exp = new ArrayList<Expression>();
	public static Identifier S;
	public static Identifier e = new Identifier(true,"e");
	public static Identifier dollar = new Identifier(true,"$");
	public static ItemCollection i0;
	public static HashMap<ItemCollection,ItemCollection> DFA = new HashMap<ItemCollection,ItemCollection>();
	
	public static void main(String[] args) {
		getInput();
		Identifier.getFirst();
		//�ع��ķ�
		Expression tempe = new Expression();
		{
			Identifier temp = new Identifier(false,"S!");
			iset.put(temp, temp);
			tempe.left = temp;
			tempe.right.add(S);
			S = temp;
			exp.add(tempe);
		}
		//��ʼ��Ŀ��
		i0 = new ItemCollection();
		extendedExpression tempee = new extendedExpression(tempe, dollar);
		i0.list.put(tempee, tempee);
		i0.getClosure();
		DFA.put(i0, i0);
		boolean changed = false;
		do {
			changed = false;
			Iterator<ItemCollection> iic = DFA.keySet().iterator();
			while (iic.hasNext()) {//����ÿ����Ŀ��
				ItemCollection tempic = iic.next();
				Iterator<extendedExpression> iee = tempic.list.keySet().iterator();
				while (iee.hasNext()) {//����һ����Ŀ���������Ŀ
					extendedExpression oldTemp = iee.next();
					extendedExpression newTemp = oldTemp.go();
					if (newTemp != null) {
						ItemCollection next = null;
						if (tempic.next.containsKey(newTemp.passed)) {
							next = tempic.next.get(newTemp.passed);
							next.list.put(newTemp, newTemp);
						}
						else {
							next = new ItemCollection();
							next.list.put(newTemp, newTemp);
							tempic.next.put(newTemp.passed, next);
						}
					}
				}
				//����next�������е���Ŀ����closure���͹淶��ϲ�ȥ��
				Set<Identifier> temps = tempic.next.keySet();
				Iterator<Identifier> tempii = temps.iterator();
				while (tempii.hasNext()) {
					Identifier tempi = tempii.next();
					ItemCollection nextic = tempic.next.get(tempi);
					nextic.getClosure();
					if (!DFA.containsKey(nextic)) {
						DFA.put(nextic, nextic);
						changed = true;
						iic = DFA.keySet().iterator();
					}
					else tempic.next.put(tempi, DFA.get(nextic));
				}
			}
		} while (changed);
		
		Hashtable<pair,result> temphm = LR1sheet();
		
		//����
		System.out.print("\nʾ������:( ( ( ( 2 + 4 ) * 8 ) / 5 ) + 0 )\n������������Ŵ�w(�ո�ָ�ÿ������):");
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
		//��ʼ����
		try {
			if (!LR1Analysis(in, temphm))
				System.out.println("�����Ƴ�����ʽ");
		} catch (Exception ex) {
			//ex.printStackTrace();
			System.out.println("���ִ��󣬲����Ƴ�����ʽ");
		}
	}
	
	public static Hashtable<pair,result> LR1sheet() {
		Hashtable<pair,result> rs = new Hashtable<pair,result>();
		Iterator<ItemCollection> iic = DFA.keySet().iterator();
		while (iic.hasNext()) {
			ItemCollection ic = iic.next();
			Iterator<extendedExpression> iee = ic.list.keySet().iterator();
			while (iee.hasNext()) {
				extendedExpression tempee = iee.next();
				if (tempee.split == tempee.right.size()) {
					//��Լ��Ŀ����acc��Ŀ
					pair tempp = new pair(ic, tempee.follow);
					result tempr = new result(null, false, tempee.parent);
					if (rs.containsKey(tempp) && !rs.get(tempp).equals(tempr)) {
						System.out.println("�����ظ�����޷�����������");
						System.exit(1);
					}
					else {
						rs.put(tempp,tempr);
						if (tempee.left == Grammar.S)
							result.accept = tempr;
					}
				}
				else {
					//shift��Ŀ//goto��Ŀ
					pair tempp = new pair(ic, tempee.right.get(tempee.split));
					result tempr = new result(ic.next.get(tempee.right.get(tempee.split)), true, null);
					if (rs.containsKey(tempp) && !rs.get(tempp).equals(tempr)) {
						System.out.println("�����ظ�����޷�����������");
						System.exit(1);
					}
					else 
						rs.put(tempp, tempr);
				}
			}
		}
		Iterator<pair> ip = rs.keySet().iterator();
		System.out.println("LR(1)���������£�\n(1)goto��ĿҲʹ��s��ʾ��ʵ�ʲ����Ӱ��\n(2)״̬����˳���ţ�����ϵͳ����");
		while (ip.hasNext()) {
			pair tempp = ip.next();
			System.out.println(tempp + "" + rs.get(tempp));
		}
		System.out.println("״̬����" + DFA.keySet().size());
		return rs;
	}
	
	public static boolean LR1Analysis(ArrayList<Identifier> in, Hashtable<pair,result> sheet) {
		Stack<Identifier> idstack = new Stack<Identifier>();
		Stack<ItemCollection> icstack = new Stack<ItemCollection>();
		icstack.push(i0);
		in.add(Grammar.dollar);
		int ip = 0;
		do {
			ItemCollection S = icstack.peek();
			Identifier a = in.get(ip);
			result tempr = sheet.get(new pair(S,a));
			if (tempr.isShift) {
				idstack.push(a);
				icstack.push(tempr.result);
				ip++;
			}
			else if (!tempr.isShift && tempr != result.accept) {
				int num = tempr.exp.right.size();
				for (int i = 0; i < num; i++) {
					idstack.pop();
					icstack.pop();
				}
				idstack.push(tempr.exp.left);
				result tempre = sheet.get(new pair(icstack.peek(),tempr.exp.left));
				icstack.push(tempre.result);
				System.out.println("ͨ��" + tempr.exp + "��Լ");
			}
			else if (!tempr.isShift && tempr == result.accept) {
				return true;
			}
			else return false;
			Iterator<Identifier> it = idstack.iterator();
			while (it.hasNext())
				System.out.print(it.next().string);
			System.out.print(".");
			for (int i = ip; i < in.size(); i++)
				System.out.print(in.get(i).string);
			System.out.println();
		} while (true);
	}
	
	public static void getInput() {
		//��ת���ַ���->��|�����ս����д��ĸ��ͷ��eΪ�գ���һ�����ʽ���ս��ΪS
		System.out.println("��ӭʹ��LR(1)�ķ���������\nMade by ������\n��������LR(1)�ķ���ÿһ��һ����һ����ֻ����һ���󲿷��ս������д��ĸ��ͷ�����Ҳ����֮����ո�����Ա�ִʡ�\n�Ƴ�����Ϊ->���߷���Ϊ|\ne�����\n������д������������\nʾ������:\nE->E + T|E - T|T\nT->T * F|T / F|F\nF->( E )|Num\nNum->0|1|2|3|4|5|6|7|8|9\n�������ķ�:\n");
		iset.put(e, e);
		iset.put(dollar, dollar);
		boolean init = true;
		while (true) {
			String buffer = input.nextLine();
			if (buffer.length() == 0)//���н���
				break;
			else {
				String[] fisrtSplit = buffer.split("->");
				if (fisrtSplit.length == 2) {//���-��
					String[] secondSplit = fisrtSplit[0].split(" ");
					if (secondSplit.length != 1 || !(secondSplit[0].length() != 0 && secondSplit[0].charAt(0) >= 'A' && secondSplit[0].charAt(0) <= 'Z'))//��߶�����Ż��������ս��
						System.out.println("���󣺷�CFG��");
					else {
						//�������
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
							for (int i = 0; i < thirdSplit.length; i++) {//��ÿһ�����Ŵ���
								if (thirdSplit[i].length() != 0) {//���Կո�
									valid = true;
									temp.left = left;
									if (thirdSplit[i].charAt(0) >= 'A' && thirdSplit[i].charAt(0) <= 'Z') //���ս��
										tempi = new Identifier(false, thirdSplit[i]);
									else tempi = new Identifier(true, thirdSplit[i]);
									if (iset.get(tempi) == null)
										iset.put(tempi, tempi);
									else tempi = iset.get(tempi);
									temp.right.add(tempi);
									
								}
							}
							if (valid) //��Ч��expression
								exp.add(temp);
							else System.out.println("��Ч�ı��ʽ");
						}
					}
				}
				else System.out.println("���󣺷��ֶ��->��");
			}
			init = false;
		}
	}
}



class ItemCollection {
	public HashMap<extendedExpression, extendedExpression> list = new HashMap<extendedExpression, extendedExpression>();
	public HashMap<Identifier,ItemCollection> next = new HashMap<Identifier,ItemCollection>();
	public int id;
	private static int idCount = 0;
	
	ItemCollection() {
		id = idCount++;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id + " List:\n");
		Iterator<extendedExpression> it = list.keySet().iterator();
		while (it.hasNext()) {
			sb.append(it.next());
		}
		Iterator<Identifier> iit = next.keySet().iterator();
		sb.append("\n");
		while (iit.hasNext()) {
			Identifier temp = iit.next();
			sb.append("��" + temp + ":" + next.get(temp).id);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void getClosure() {
		Iterator<extendedExpression> eei = list.keySet().iterator();
		while (eei.hasNext()) {
			extendedExpression tempee = eei.next();
			if (tempee.closure(this)) 
				eei = list.keySet().iterator();
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof ItemCollection) {
			ItemCollection oo = (ItemCollection)o;
			return list.equals(oo.list);
		}
		else return false;
	}
	
	public int hashCode() {
		return list.hashCode();
	}
	
}

class extendedExpression extends Expression {
	public int split = 0;//length�������
	public Identifier follow;
	public Identifier passed;
	public Expression parent;
	
	extendedExpression(Expression ex, Identifier follow) {
		this.left = ex.left;
		this.right = ex.right;
		this.follow = follow;
		while (this.right.remove(Grammar.e))
			;
		if (!(ex instanceof extendedExpression))
			this.parent = ex;
		else this.parent = ((extendedExpression)ex).parent;
	}
	extendedExpression go() {
		if (split == right.size())
			return null;
		else {
			extendedExpression temp = new extendedExpression(this,this.follow);
			temp.split = this.split + 1;
			temp.passed = this.right.get(split);
			return temp;
		}
	}
	boolean closure(ItemCollection ic) {//trueΪ�ı�
		if (split != right.size()) {
			if (right.get(split).isTerminal) //�ս��������չ��
				return false;
			else {
				boolean changed = false;
				Iterator<Expression> ie = Grammar.exp.iterator();
				while (ie.hasNext()) {
					Expression temp = ie.next();
					if (temp.left == right.get(split)) {
						Expression etemp = new Expression();
						List<Identifier> templ = ((ArrayList<Identifier>)this.right.clone()).subList(this.split + 1, this.right.size());
						templ.add(this.follow);
						etemp.right = new ArrayList<Identifier>(templ);
						Set<Identifier> temps = etemp.getFirst().keySet();
						Iterator<Identifier> ii = temps.iterator();
						while (ii.hasNext()) {
							extendedExpression eetemp = new extendedExpression(temp,ii.next());
							if (!ic.list.containsKey(eetemp)) {
								ic.list.put(eetemp, eetemp);
								changed = true;
							}
						}
					}
				}
				return changed;
			}
		}
		else return false;
		
	}
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(left.string + "->");
		for (int i = 0; i < right.size(); i++) {
			if (i == split)
				s.append('.');
			s.append(right.get(i).string);
		}
		s.append(',');
		s.append(follow.string);
		s.append('��');
		return s.toString();
	}
	public int hashCode() {
		return left.hashCode()+right.hashCode()+follow.hashCode()+split;
	}
	public boolean equals(Object o) {
		if (o instanceof extendedExpression) {
			extendedExpression oo = (extendedExpression)o;
			return split == oo.split && follow == oo.follow && left.equals(oo.left) && right.equals(oo.right);
		}
		else return false;
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
	public HashMap<Identifier,Identifier> getFirst() {//���ڶ��ض��Ҳ�����
		HashMap<Identifier,Identifier> returnResult = new HashMap<Identifier,Identifier>();
		boolean hasE;
		if (returnResult.put(Grammar.e, Grammar.e) == null)
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
				if (this.right.get(i).first.containsKey(Grammar.e))
					checkNext = true;
			}
			if (!checkNext) {
				if (!hasE)
					returnResult.remove(Grammar.e);
				break;
			}
		}
		return returnResult;
	}
}

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
		ArrayList<Expression> exp = Grammar.exp;
		do {
			changed = false;
			Iterator<Expression> it = exp.iterator();
			while (it.hasNext()) {
				Expression temp = it.next();
				Identifier templ = temp.left;
				boolean hasE;
				if (temp.left.first.put(Grammar.e, Grammar.e) == null)
					hasE = false;
				else hasE = true;
				
				for (int i = 0; i < temp.right.size(); i++) {
					boolean checkNext = false;
					if (temp.right.get(i).isTerminal && temp.right.get(i).string.equals("e")) {
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
						if (temp.right.get(i).first.containsKey(Grammar.e))
							checkNext = true;
					}
					if (i == temp.right.size() - 1) {
						if (checkNext && !hasE) 
							changed = true;
						else if (!checkNext && !hasE)
							temp.left.first.remove(Grammar.e);
					}
					else if (!checkNext) {
						if (!hasE)
							temp.left.first.remove(Grammar.e);
						break;
					}
				}
			}
		} while (changed);
	}
}



class result {
	ItemCollection result;
	boolean isShift;
	Expression exp;
	static result accept;
	
	public result(ItemCollection result,boolean isShift,Expression exp) {
		this.result = result;
		this.isShift = isShift;
		this.exp = exp;
	}
	
	public String toString() {
		return (isShift ? "s" + result.id : "r" + exp + (exp.right.size() == 0 ? "��" : "")) ;
	}
	
	public int hashCode() {
		if (isShift)
			return result.hashCode();
		else return exp.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof result) {
			if (isShift)
				return result == ((result)o).result;
			else return exp == ((result)o).exp;
		}
		else return false;
	}
}
