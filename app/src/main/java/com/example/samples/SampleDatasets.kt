package com.example.samples

data class SampleCsv(
  val title: String,
  val description: String,
  val fileName: String,
  val csvContent: String,
  val defaultDelimiter: Char = ','
)

object SampleDatasets {

  val SAMPLES = listOf(
    SampleCsv(
      title = "Quarterly Sales Report",
      description = "Regional sales records, revenue, units sold, and profit",
      fileName = "quarterly_sales.csv",
      csvContent = """
        Region,Representative,Product Category,Units Sold,Unit Price,Revenue,Profit Margin,Status
        North America,Sarah Jenkins,Enterprise Cloud,142,499.00,70858.00,0.42,Completed
        North America,Michael Scott,Office Hardware,320,129.50,41440.00,0.28,Completed
        Europe,Emma Watson,Developer Tools,210,249.00,52290.00,0.51,Completed
        Asia Pacific,Kenji Sato,Enterprise Cloud,98,499.00,48902.00,0.44,In Review
        Europe,Liam O'Connor,Cybersecurity Suite,75,899.00,67425.00,0.60,Completed
        Latin America,Sofia Rodriguez,Office Hardware,180,129.50,23310.00,0.25,Pending
        Asia Pacific,Priya Sharma,Developer Tools,340,249.00,84660.00,0.53,Completed
        North America,David Miller,Cybersecurity Suite,115,899.00,103385.00,0.62,Completed
        Europe,Hans Gruber,Enterprise Cloud,64,499.00,31936.00,0.39,Completed
        Latin America,Mateo Gomez,Developer Tools,125,249.00,31125.00,0.48,Completed
      """.trimIndent()
    ),
    SampleCsv(
      title = "Employee Directory",
      description = "Team members, departments, roles, salaries, and hire dates",
      fileName = "employee_directory.csv",
      csvContent = """
        Employee ID,Full Name,Department,Role,Email,Hire Date,Annual Salary,Active
        EMP-1001,Elena Rostova,Engineering,Principal Architect,elena.r@company.io,2021-03-15,178000,true
        EMP-1002,Marcus Vance,Product,Lead Product Manager,marcus.v@company.io,2022-06-01,152000,true
        EMP-1003,Amina Diallo,Design,Senior UX Designer,amina.d@company.io,2022-09-12,126000,true
        EMP-1004,David Chen,Engineering,Senior Backend Engineer,david.c@company.io,2020-11-20,145000,true
        EMP-1005,Rachel Green,Marketing,Content Strategist,rachel.g@company.io,2023-01-10,92000,true
        EMP-1006,Lucas Moreau,Operations,Logistics Coordinator,lucas.m@company.io,2023-04-05,74000,true
        EMP-1007,Pooja Patel,Finance,Financial Analyst,pooja.p@company.io,2021-08-18,105000,true
        EMP-1008,James Wilson,Support,Customer Success Lead,james.w@company.io,2022-02-28,88000,true
      """.trimIndent()
    ),
    SampleCsv(
      title = "Product Inventory",
      description = "Warehouse stock, SKU codes, pricing, and reorder alerts",
      fileName = "product_inventory.csv",
      csvContent = """
        SKU,Product Name,Category,Quantity In Stock,Reorder Level,Unit Cost,Retail Price,Supplier
        TECH-8821,Ergonomic Mechanical Keyboard,Electronics,240,50,42.50,119.99,Apex Tech Corp
        TECH-3419,Wireless Vertical Mouse,Electronics,480,100,18.20,54.99,Apex Tech Corp
        OFF-5510,Aluminum Laptop Stand,Accessories,150,40,14.80,39.99,Zenith Goods
        OFF-9021,Noise-Cancelling Desk Mat,Accessories,620,150,7.90,24.99,Zenith Goods
        MON-1004,27-inch 4K IPS Monitor,Displays,85,25,185.00,349.99,Optix Vision Ltd
        MON-2008,Dual Monitor Arm Mount,Accessories,190,60,28.40,79.99,Optix Vision Ltd
        CAB-4411,Thunderbolt 4 Docking Station,Electronics,110,35,74.00,189.99,ConnectPro Co
        CAB-1200,Braided USB-C Cable (2m),Cables,1200,300,2.10,12.99,ConnectPro Co
      """.trimIndent()
    ),
    SampleCsv(
      title = "Financial Transactions (Semicolon Delimited)",
      description = "European format with semicolon delimiters and dates",
      fileName = "financial_records.csv",
      defaultDelimiter = ';',
      csvContent = """
        Transaction ID;Date;Description;Category;Amount EUR;Payment Method;Tax Deductible
        TXN-90812;2024-05-01;AWS Cloud Infrastructure;Hosting;1420.50;Corporate Credit;true
        TXN-90813;2024-05-03;Figma Enterprise Seats;Software;540.00;Corporate Credit;true
        TXN-90814;2024-05-05;Client Dinner Zurich;Business Meal;325.80;Corporate Card;true
        TXN-90815;2024-05-08;High-Speed Office Fiber;Utilities;189.00;Direct Debit;true
        TXN-90816;2024-05-12;Annual SSL Certificates;Security;210.00;PayPal;true
        TXN-90817;2024-05-15;Google Workspace Business;Productivity;480.00;Corporate Credit;true
        TXN-90818;2024-05-20;Ergonomic Chairs (x4);Office Furniture;1120.00;Bank Transfer;true
      """.trimIndent()
    )
  )
}
