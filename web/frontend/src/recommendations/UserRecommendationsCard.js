import React from 'react'

import { Card, Table } from 'antd'

function UserRecommendationsCard({
  inactive,
  personId,
  personName,
  recommendations,
}) {
  return (
    <Card title={`${personName} (#${personId})`} size="small">
      <Table dataSource={recommendations} pagination={false} size="small">
        <Table.Column
          title="ID"
          dataIndex="friendPersonId"
          key="friendPersonId"
        />
        <Table.Column
          title="Name"
          dataIndex="friendPersonName"
          key="friendPersonName"
        />
        <Table.Column
          title="Similarity"
          dataIndex="similarity"
          key="similarity"
          align="right"
        />
      </Table>
    </Card>
  )
}

export default UserRecommendationsCard
