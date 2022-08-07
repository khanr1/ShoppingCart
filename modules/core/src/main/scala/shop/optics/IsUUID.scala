package shop.optics

import monocle.Iso
import java.util.UUID


trait IsUUID[A] {
  def _UUID:Iso[UUID,A]
}

object IsUUID{
  def apply[A:IsUUID]:IsUUID[A]=summon
  
}