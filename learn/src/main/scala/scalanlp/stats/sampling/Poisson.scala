package scalanlp.stats.sampling;

/*
 Copyright 2009 David Hall, Daniel Ramage
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at 
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
*/

import scalala.library.Numerics._;
import math._;

/**
 * Represents a Poisson random variable.
 * @author dlwh
 */
class Poisson(val mean: Double)(implicit rand: RandBasis=Rand) extends DiscreteDistr[Int] with Moments[Double] {
  private val ell = math.exp(-mean);
  // impl from winrand
  def draw():Int = {
    if(mean == 0) 0
    else if(mean < 10.0) { // small
      var t = ell;
      var k = 0;
      val u = rand.uniform.get;
      var s = t;
      while(s < u) {
        k += 1;
        t *= mean / k;
        s += t;
      }
      k
    } else {
      val k_start = mean.toInt;
      val u = rand.uniform.get;
      var t1 = exp(k_start * log(mean) - mean - lgamma(k_start+1));
      if (t1 > u) k_start
      else {
        var k1 = k_start;
        var k2 = k_start;
        var t2 = t1;
        var s = t1;
        while(true) {
          k1 += 1;
          t1 *= mean / k1; s += t1;
          if (s > u) return k1;
          if (k2 > 0) {
            t2 *= k2 / mean;
            k2 -= 1;
            s += t2;
            if (s > u) return k2;
          }
        }
        error("wtf");
      }

    }
  }

  def probabilityOf(k:Int) = math.exp(logProbabilityOf(k));
  override def logProbabilityOf(k:Int) = {
    -mean + k * log(mean) - lgamma(k+1);
  }

  def logCdf(k: Int) = lgamma(k+1,mean) - lgamma(k+1);
  def cdf(k:Int) = exp(logCdf(k));

  def variance = mean;
}
