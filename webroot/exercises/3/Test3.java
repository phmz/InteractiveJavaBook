public classTest3{
	@Test
	public void testRemoveSpaces(){
		assertEquals(removeSpaces("  ciao  "), "ciao");
		assertEquals(removeSpaces("sal ut "), "salut");
		assertEquals(removeSpaces("  tr  im  "), "trim"); 
	}
}