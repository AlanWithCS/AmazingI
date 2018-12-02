package test;

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public TreeNode upsideDownBinaryTree(TreeNode root) {
        if (root == null) return root;
        TreeNode node = root;
        while (node.left != null) { node = node.left; }
        helper(root);
        return node;  
    }
    
    private TreeNode helper(TreeNode root) {
        if (root == null || root.left == null) return root; // when left==null, right==null
        System.out.println("upsidedown:"+root.val);
        TreeNode newRoot = upsideDownBinaryTree(root.left);
        
        newRoot.right = root;
        newRoot.left = root.right;
        System.out.println("root:"+root.val);
        System.out.println(newRoot.val+" right: "+root.val);
        System.out.println(newRoot.val+" left: "+root.right.val);
        root.left = null;
        root.right = null;
        System.out.println("return"+root.val);
        return newRoot;
    }
    
    public static class TreeNode {
		int val;
		TreeNode left;
		TreeNode right;
		TreeNode(int x) { val = x; }
    }
    
    public static void main(String[] args) {
    	char[] temp = new char[4];
    	for (char c : temp) {
    		System.out.println(c);
    	}
    	
    	
    }
    
    
}








