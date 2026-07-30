const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
  const users = await prisma.user.findMany({
    include: {
      _count: {
        select: { transactions: true, budgets: true, savingsGoals: true }
      }
    }
  });
  console.log("Users in DB:");
  users.forEach(u => {
    console.log(`- ID: ${u.id}, Email: ${u.email}, Role: ${u.role}, Tx Count: ${u._count.transactions}, Budget Count: ${u._count.budgets}`);
  });

  const transactions = await prisma.transaction.findMany({
    take: 10,
    include: { category: true }
  });
  console.log("\nSome Transactions:");
  transactions.forEach(t => {
    console.log(`- User: ${t.userId}, Amount: ${t.amount}, Type: ${t.type}, Date: ${t.transactionDate.toISOString().split('T')[0]}, Cat: ${t.category.name}`);
  });
}

main().catch(console.error).finally(() => prisma.$disconnect());
