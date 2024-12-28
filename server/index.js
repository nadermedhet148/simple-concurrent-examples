import express from 'express';

const app = express();
const port = process.env.PORT || 3000;
const serverId = process.env.SERVER_ID;

let requestCount = 0;

app.get('/*', async (req, res) => {
  const delay = Math.random() * Math.random() * 10000;
  requestCount++;
  console.log(`Request count: ${requestCount}`);
  await new Promise(resolve => setTimeout(resolve, delay));
  res.send({
    message: 'Hello World!!',
    serverId: serverId,
    requestCount: requestCount,
  });
});

app.listen(port, () => {
  console.log(`Server listening at ${port}`);
});
