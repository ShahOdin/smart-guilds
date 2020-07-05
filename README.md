# Smart Guilds
a poc for contractors to come together and doing away with recruiters

## Background

### Problem

Currently freelancer professionals often have to manage finances, marketing and all the associated aspects of running a business themselves. This is arguably fine if the market is a saturated one: freelancers compete with each other, free market economics, etc. etc. Otherwise the situation is not ideal :

- Many professionals don't choose freelancing because they have an arbitrary understanding of the market, their skills and their values in the market.

- Many freelancers undervalue their service as they lack the sales and marketing skills.

- Some freelancers overcharge for their service simply because they are better at marketing themselves or have better connections. This is bad for contractees but also bad for other freelancers who might be at an "unfair" disadvantage.

- Many freelancers have to depend on recruiters or umbrella companies to outsource the non-technical aspect of their work and don't get the full money they charge.

### Imperfect solution

Ideally, freelancers would want a cooperative of some sort that can:

- offer contractees the best value for their money

- offer contractors the highest price for their service.

- charge minimum admin fee.

Guilds or cooperatives were historically "an association of artisans or merchants who oversee the practice of their craft/trade in a particular area." In reality, running a cooperative is hard:

- There is need for maximum trust and transparency for handling the paperwork which is a task on its own.

- Different members of the coop, might have different standards of service and coming to a conclusion on who should get how much can be challenging.

- Coops are tasked with both finding work as well as assigning take-home salaries for their members. finding the balance can be tricky:

    - A coop might decide to merely be a proxy for awarding contracts and let the contractor take all the money for the contract they are on. This might create an uncomfortable atmosphere, where the unemployed me might feel jealous and resentful of a colleague on a juicy contract.
    
    - On the other end of the spectrum, one can imagine a situation where all the money charged for the services goes into the same pot and is distributed among members. This could be demoralising and a barrier for highly skilled professionals joining a guild. As such they might give up the other benefits of the coop.

### Automation and Smart contracts to the rescue?

At long last, I was recently exposed to smart contracts through [Youtube](https://www.youtube.com/watch?v=ZE2HxTmxfrI)! And I thought it'd be a good idea to mathematically review the challenges above and turn the different strategies into different algorithms. Run them and see how they reward the guild members financially. Hypothetical guild members can then adopt one of these strategies and use them in their smart contracts.

## Concepts

The following is the public interface for the cooperation:

```scala 
  trait Cooperation {
    def hireContractor(dailyRate: DailyRate): Option[Contractor]
    def retireContractor(contractor: Contractor): Unit

    def incrementRating(contractor: Contractor): Unit
    def decrementRating(contractor: Contractor): Unit
    def payForService(contractor: Contractor, dailyRate: DailyRate): Unit
  }
```

The idea is that contractor's ratings can determine their chances of getting work and potentially, how much they are paid. when it gets to the implementation, the two main logical components are:

- who is allocated a role:
  
```scala 
    def hireContractor(dailyRate: DailyRate): Option[Contractor]
```

- how the money is distributed to contractors
  
```scala
    def payForService(contractor: Contractor, dailyRate: DailyRate): Unit
```

## Demo

A Very basic demo is done in the [Demo app](smart-guilds/src/Demo.scala).

## Agenda
 
 - A default implementation for the hiring process is chosen. the assumptions need to be reviewed and alternatives should be considered.
 - Different `Cooperation` implementations such as `communism`, `ubiPercentage` and `minimal`  etc are provided for comparison.
 - There is no concept of time yet, so effectively all contractors are always available to pick up new contracts. which is a terrible flaw in modelling.
 - Once the modelling supports dynamic concept of time, we can compare and plot the income of our contractors over time and see how it works for them.
 - Potentially next, we can consider the impact of the market situation on the incomes. and compare the benefits of being in a Coop, as opposed to working alone. 
