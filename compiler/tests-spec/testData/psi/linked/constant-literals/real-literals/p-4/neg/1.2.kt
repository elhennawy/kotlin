/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-draft
 * PLACES: constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals with dots separated by underscores.
 */

val value = _._.0_0f
val value = ._.0__0___0e1
val value = ....0_0__0_0f

val value = .._0
val value = .______.0______0
val value = _______.________00.0_0_0F

val value = _001.._.0_0e10f
val value = _4_.___.33__3.00__0E-10
val value = __4_._44_.__._4.00___.___00E0
val value = ____._____5.5.5_________5.0E-0
val value = _._666_666.0____________.__________________________________________________________________________________________________________0e-0000000f
val value = ____._.0_0__.0F
val value = _.8_.8_.8_.8_.8_.8_.8_.8_.0000
val value = .____.____9____.___9_____9____9___9__9_9.0f

val value = _0_0_0_.0_0_0_0_0_0._.1234567890
val value = ._9e-._10
val value = __._234_.5_678.345______________678E+000001000000____
val value = _._._3_456_7.4567E
val value = _456.5_6F
val value = _5.6_.0.5
val value = ___6_54.7654e-0_.0
val value = _7_6543.87654_3E+
val value = ___._________._________876543_____________2.9_____________8765.432E-1100F
val value = _.0_.9_____________87654321.098765432_____________1ef

val value = _.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_0F
val value = _______________0._.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e-10
val value = ___99999999999999999999.99999999999999999999999_______.________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E0
