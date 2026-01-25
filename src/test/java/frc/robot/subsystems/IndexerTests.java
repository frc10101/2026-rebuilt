package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IndexerTests {
  private Indexer mIndexer;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mIndexer = new Indexer();
  }

  @Test
  void canCreateIndexerTest() {
    assertNotNull(mIndexer);
  }
}
