import chisel3._
import chisel3.util._
import chisel3.simulator.ChiselSim
import chisel3.simulator.Settings
import org.scalatest.flatspec.AnyFlatSpec
import svsim.verilator._
//import tywaves.simulator.simulatorSettings._
//import tywaves.simulator.ParametricSimulator._

class RegisterFooSpec extends AnyFlatSpec with ChiselSim {
    val enableWavesAtTiemZero = true
    //class Vcd extends Type

    "RegisterFoo" should "store and output the input value" in {
        simulate(
            new RegisterFoo(32),


            // settings를 `chisel3.simulator.Settings` 객체로 변경합니다.
            settings = Settings.defaultRaw[RegisterFoo].copy(
                backendSettings = Some( // Some()으로 감싸는 것은 backendSettings가 Option 타입일 가능성이 높기 때문입니다.
                    svsim.verilator.Backend.CompilationSettings(
                        traceStyle = Some(
                            svsim.verilator.Backend.CompilationSettings.TraceStyle(
                                // kind를 `new Vcd with Type` 대신 `svsim.verilator.Backend.CompilationSettings.TraceKind.Vcd`로 변경합니다.
                                kind = svsim.verilator.Backend.CompilationSettings.TraceKind.Vcd,
                                traceUnderscore = true
                            )
                        )
                    )
                )


            )
            ) { dut =>
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



























