package myproject

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

class RegisterSpec extends AnyFlatSpec with ChiselSim {
  "Register" should "store and output the input value" in {
    simulate(new Register(32)) { dut =>
      // Test case 1: Basic write and read
      dut.io.in.poke(123.U)
      dut.clock.step(1)
      dut.io.out.expect(123.U)

      // Test case 2: Write a different value
      dut.io.in.poke(45678.U)
      dut.clock.step(1)
      dut.io.out.expect(45678.U)

      // Test case 3: Write maximum 32-bit unsigned value
      dut.io.in.poke("hFFFFFFFF".U(32.W))
      dut.clock.step(1)
      dut.io.out.expect("hFFFFFFFF".U(32.W))

      // Test case 4: Write 0
      dut.io.in.poke(0.U)
      dut.clock.step(1)
      dut.io.out.expect(0.U)
    }
  }
}
