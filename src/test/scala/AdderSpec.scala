package myproject

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

class AdderSpec extends AnyFlatSpec with ChiselSim {
  
  "Adder" should "add two numbers" in {
    simulate(new Adder(32)) { dut =>
      // Test case 1: Basic addition
      dut.io.a.poke(1.U(32.W))
      dut.io.b.poke(2.U(32.W))
      dut.clock.step(1)
      dut.io.sum.expect(3.U(32.W))

      // Test case 2: Add with zero
      dut.io.a.poke(5.U(32.W))
      dut.io.b.poke(0.U(32.W))
      dut.clock.step(1)
      dut.io.sum.expect(5.U(32.W))

      // Test case 3: Larger numbers
      dut.io.a.poke(100000.U(32.W))
      dut.io.b.poke(200000.U(32.W))
      dut.clock.step(1)
      dut.io.sum.expect(300000.U(32.W))

      // Test case 4: Overflow (max 32-bit unsigned + 1)
      dut.io.a.poke("hFFFFFFFF".U(32.W))
      dut.io.b.poke(1.U(32.W))
      dut.clock.step(1)
      dut.io.sum.expect(0.U(32.W))

      // Test case 5: Another overflow scenario
      dut.io.a.poke("hFFFFFFFE".U(32.W))
      dut.io.b.poke(2.U(32.W))
      dut.clock.step(1)
      dut.io.sum.expect(0.U(32.W))
    }
  }
}
